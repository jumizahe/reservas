import React, { useEffect, useState, useCallback } from 'react'
import { listAppointments, confirmAppointment, cancelAppointment, markNoShow } from '../api/appointments'
import Pagination from '../components/Pagination'
import dayjs from 'dayjs'

const STATUS_MAP = {
  SCHEDULED: { label: 'Programada', class: 'badge-scheduled' },
  CONFIRMED: { label: 'Confirmada', class: 'badge-confirmed' },
  COMPLETED: { label: 'Completada', class: 'badge-completed' },
  CANCELLED: { label: 'Cancelada', class: 'badge-cancelled' },
  NO_SHOW: { label: 'No-show', class: 'badge-noshow' },
}

function StatusBadge({ status }) {
  const info = STATUS_MAP[status] || { label: status, class: '' }
  return <span className={`badge-status ${info.class}`}>{info.label}</span>
}

export default function AppointmentsList() {
  const [data, setData] = useState({ content: [], totalPages: 1, number: 0 })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [actionLoading, setActionLoading] = useState(null)

  const loadAppointments = useCallback(async (page = 0) => {
    setLoading(true)
    setError(null)
    try {
      const res = await listAppointments(page, 10)
      setData(res.data)
    } catch (e) {
      setError(e.message || 'Error al cargar las citas')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { loadAppointments(0) }, [loadAppointments])

  const handleConfirm = async (id) => {
    setActionLoading(id)
    try {
      await confirmAppointment(id)
      await loadAppointments(data.number)
    } catch (e) {
      setError(e.message || 'Error al confirmar la cita')
    } finally {
      setActionLoading(null)
    }
  }

  const handleCancel = async (id) => {
    const reason = prompt('Motivo de cancelación:')
    if (!reason) return
    setActionLoading(id)
    try {
      await cancelAppointment(id, reason)
      await loadAppointments(data.number)
    } catch (e) {
      setError(e.message || 'Error al cancelar la cita')
    } finally {
      setActionLoading(null)
    }
  }

  const handleNoShow = async (id) => {
    if (!window.confirm('¿Marcar como no-show?')) return
    setActionLoading(id)
    try {
      await markNoShow(id)
      await loadAppointments(data.number)
    } catch (e) {
      setError(e.message || 'Error al marcar no-show')
    } finally {
      setActionLoading(null)
    }
  }

  const canAct = (status) => ['SCHEDULED', 'CONFIRMED'].includes(status)

  return (
    <div>
      <div className="page-header">
        <h2>Citas</h2>
        <p>Gestión de citas médicas</p>
      </div>

      {error && (
        <div className="alert alert-danger d-flex align-items-center justify-content-between mb-3">
          <span>{error}</span>
          <button className="btn btn-sm btn-ghost" onClick={() => setError(null)}>Cerrar</button>
        </div>
      )}

      {loading ? (
        <div className="spinner-container"><div className="spinner" /></div>
      ) : data.content.length === 0 ? (
        <div className="card">
          <div className="empty-state">
            <div className="empty-icon">{'\u{1F4C5}'}</div>
            <p>No hay citas registradas</p>
          </div>
        </div>
      ) : (
        <div className="card">
          <div style={{ overflowX: 'auto' }}>
            <table className="table table-hover">
              <thead>
                <tr>
                  <th>Paciente</th>
                  <th>Doctor</th>
                  <th>Consultorio</th>
                  <th>Tipo</th>
                  <th>Inicio</th>
                  <th>Fin</th>
                  <th>Estado</th>
                  <th style={{ textAlign: 'right' }}>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {data.content.map(a => (
                  <tr key={a.id}>
                    <td style={{ fontWeight: 500 }}>{a.patientName}</td>
                    <td>{a.doctorName}</td>
                    <td>{a.officeName}</td>
                    <td>{a.appointmentTypeName}</td>
                    <td>{dayjs(a.startAt).format('DD/MM/YYYY HH:mm')}</td>
                    <td>{dayjs(a.endAt).format('DD/MM/YYYY HH:mm')}</td>
                    <td><StatusBadge status={a.status} /></td>
                    <td style={{ textAlign: 'right' }}>
                      {canAct(a.status) && (
                        <div className="d-flex gap-1 justify-content-end">
                          <button
                            className="btn btn-sm btn-ghost"
                            style={{ color: 'var(--success)' }}
                            onClick={() => handleConfirm(a.id)}
                            disabled={actionLoading === a.id}
                          >
                            Confirmar
                          </button>
                          <button
                            className="btn btn-sm btn-ghost"
                            style={{ color: 'var(--danger)' }}
                            onClick={() => handleCancel(a.id)}
                            disabled={actionLoading === a.id}
                          >
                            Cancelar
                          </button>
                          <button
                            className="btn btn-sm btn-ghost"
                            style={{ color: 'var(--warning)' }}
                            onClick={() => handleNoShow(a.id)}
                            disabled={actionLoading === a.id}
                          >
                            No-show
                          </button>
                        </div>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="d-flex justify-content-center py-3" style={{ borderTop: '1px solid var(--border)' }}>
            <Pagination page={data.number} totalPages={data.totalPages} onPage={loadAppointments} />
          </div>
        </div>
      )}
    </div>
  )
}
