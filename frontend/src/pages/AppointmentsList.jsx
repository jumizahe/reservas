import React, { useEffect, useState } from 'react'
import { listAppointments, confirmAppointment, cancelAppointment, markNoShow } from '../api/appointments'
import dayjs from 'dayjs'

function Pagination({ page, totalPages, onPage }) {
  return (
    <nav>
      <ul className="pagination">
        <li className={`page-item ${page <= 0 ? 'disabled' : ''}`}>
          <button className="page-link" onClick={() => onPage(page - 1)}>Anterior</button>
        </li>
        <li className="page-item disabled"><span className="page-link">{page + 1} / {totalPages}</span></li>
        <li className={`page-item ${page + 1 >= totalPages ? 'disabled' : ''}`}>
          <button className="page-link" onClick={() => onPage(page + 1)}>Siguiente</button>
        </li>
      </ul>
    </nav>
  )
}

export default function AppointmentsList() {
  const [data, setData] = useState({ content: [], totalPages: 1, number: 0 })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const fetch = async (page = 0) => {
    setLoading(true)
    setError(null)
    try {
      const res = await listAppointments(page, 10)
      setData(res.data)
    } catch (e) {
      setError(e.message || JSON.stringify(e))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetch(0) }, [])

  const handleConfirm = async (id) => {
    try { await confirmAppointment(id); fetch(data.number) } catch (e) { alert(e.message || 'Error') }
  }

  const handleCancel = async (id) => {
    const reason = prompt('Motivo de cancelación:')
    if (!reason) return
    try { await cancelAppointment(id, reason); fetch(data.number) } catch (e) { alert(e.message || 'Error') }
  }

  const handleNoShow = async (id) => {
    if (!window.confirm('Marcar como no-show?')) return
    try { await markNoShow(id); fetch(data.number) } catch (e) { alert(e.message || 'Error') }
  }

  return (
    <div>
      <h2 className="h5 mb-3">Citas</h2>
      {error && <div className="alert alert-danger">{error}</div>}
      {loading ? <div>Cargando...</div> : (
        <>
          <table className="table table-sm">
            <thead>
              <tr>
                <th>ID</th>
                <th>Paciente</th>
                <th>Doctor</th>
                <th>Oficina</th>
                <th>Tipo</th>
                <th>Inicio</th>
                <th>Fin</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map(a => (
                <tr key={a.id}>
                  <td>{a.id}</td>
                  <td>{a.patientName}</td>
                  <td>{a.doctorName}</td>
                  <td>{a.officeName}</td>
                  <td>{a.appointmentTypeName}</td>
                  <td>{dayjs(a.startAt).format('YYYY-MM-DD HH:mm')}</td>
                  <td>{dayjs(a.endAt).format('YYYY-MM-DD HH:mm')}</td>
                  <td>{a.status}</td>
                  <td>
                    <div className="btn-group" role="group">
                      <button className="btn btn-sm btn-success" onClick={() => handleConfirm(a.id)}>Confirmar</button>
                      <button className="btn btn-sm btn-warning" onClick={() => handleCancel(a.id)}>Cancelar</button>
                      <button className="btn btn-sm btn-danger" onClick={() => handleNoShow(a.id)}>No-show</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <Pagination page={data.number} totalPages={data.totalPages} onPage={(p) => fetch(p)} />
        </>
      )}
    </div>
  )
}
