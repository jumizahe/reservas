import React, { useEffect, useState, useCallback } from 'react'
import { listDoctors, createDoctor, updateDoctor } from '../api/doctors'
import Pagination from '../components/Pagination'
import dayjs from 'dayjs'

function DoctorForm({ doctor, specialties, onSave, onCancel }) {
  const [form, setForm] = useState({
    fullName: doctor?.fullName || '',
    email: doctor?.email || '',
    licenseNumber: doctor?.licenseNumber || '',
    specialtyId: doctor?.specialtyId || '',
  })
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.fullName || !form.email || !form.specialtyId) {
      setError('Completa todos los campos obligatorios')
      return
    }
    setSaving(true)
    setError(null)
    try {
      await onSave({ ...form, specialtyId: Number(form.specialtyId) })
    } catch (err) {
      setError(err.message || 'Error al guardar')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="card mb-4">
      <div className="card-header-custom">
        <h3>{doctor ? 'Editar doctor' : 'Nuevo doctor'}</h3>
        <button className="btn btn-sm btn-ghost" onClick={onCancel}>Cancelar</button>
      </div>
      <div className="card-body-custom">
        {error && <div className="alert alert-danger">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="row g-3">
            <div className="col-md-6">
              <label className="form-label">Nombre completo *</label>
              <input className="form-control" value={form.fullName} onChange={e => setForm({ ...form, fullName: e.target.value })} required />
            </div>
            <div className="col-md-6">
              <label className="form-label">Email *</label>
              <input className="form-control" type="email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} required />
            </div>
            <div className="col-md-6">
              <label className="form-label">Licencia</label>
              <input className="form-control" value={form.licenseNumber} onChange={e => setForm({ ...form, licenseNumber: e.target.value })} />
            </div>
            <div className="col-md-6">
              <label className="form-label">Especialidad *</label>
              <select className="form-select" value={form.specialtyId} onChange={e => setForm({ ...form, specialtyId: e.target.value })} required>
                <option value="">Seleccionar especialidad</option>
                {specialties.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
              </select>
            </div>
          </div>
          <div className="d-flex gap-2 mt-4">
            <button className="btn btn-primary" type="submit" disabled={saving}>
              {saving ? 'Guardando...' : 'Guardar'}
            </button>
            <button className="btn btn-ghost" type="button" onClick={onCancel}>Cancelar</button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default function Doctors() {
  const [data, setData] = useState({ content: [], number: 0, totalPages: 1 })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [editor, setEditor] = useState(null)
  const [specialties, setSpecialties] = useState([])
  const [search, setSearch] = useState('')

  const loadDoctors = useCallback(async (page = 0) => {
    setLoading(true)
    setError(null)
    try {
      if (search.trim()) {
        const res = await listDoctors(0, 200)
        const filtered = res.data.content.filter(d =>
          d.fullName?.toLowerCase().includes(search.toLowerCase()) ||
          d.email?.toLowerCase().includes(search.toLowerCase())
        )
        setData({ content: filtered, number: 0, totalPages: 1 })
      } else {
        const res = await listDoctors(page, 10)
        setData(res.data)
      }
    } catch (e) {
      setError(e.message || 'Error al cargar doctores')
    } finally {
      setLoading(false)
    }
  }, [search])

  useEffect(() => { loadDoctors(0) }, [loadDoctors])

  useEffect(() => {
    fetch('/api/specialties').then(r => r.json()).then(setSpecialties).catch(() => {})
  }, [])

  const handleSave = async (payload) => {
    if (editor.mode === 'create') await createDoctor(payload)
    else await updateDoctor(editor.doctor.id, payload)
    setEditor(null)
    await loadDoctors(data.number)
  }

  const handleToggleActive = async (doctor) => {
    const willActivate = !doctor.active
    if (!window.confirm(willActivate ? '¿Activar doctor?' : '¿Desactivar doctor?')) return
    try {
      await updateDoctor(doctor.id, { active: willActivate })
      await loadDoctors(data.number)
    } catch (e) {
      setError(e.message || 'Error al actualizar')
    }
  }

  return (
    <div>
      <div className="page-header d-flex justify-content-between align-items-start">
        <div>
          <h2>Doctores</h2>
          <p>Gestión de doctores y especialidades</p>
        </div>
        <button className="btn btn-primary" onClick={() => setEditor({ mode: 'create' })}>
          {'+ Nuevo Doctor'}
        </button>
      </div>

      {editor && (
        <DoctorForm
          doctor={editor.mode === 'edit' ? editor.doctor : null}
          specialties={specialties}
          onSave={handleSave}
          onCancel={() => setEditor(null)}
        />
      )}

      {error && (
        <div className="alert alert-danger d-flex align-items-center justify-content-between mb-3">
          <span>{error}</span>
          <button className="btn btn-sm btn-ghost" onClick={() => setError(null)}>Cerrar</button>
        </div>
      )}

      <div className="filters-bar">
        <div className="d-flex gap-2">
          <input
            className="form-control"
            placeholder="Buscar por nombre o email..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && loadDoctors(0)}
          />
          <button className="btn btn-ghost" onClick={() => loadDoctors(0)}>Buscar</button>
        </div>
      </div>

      {loading ? (
        <div className="spinner-container"><div className="spinner" /></div>
      ) : data.content.length === 0 ? (
        <div className="card">
          <div className="empty-state">
            <div className="empty-icon">{'\u{1FA7A}'}</div>
            <p>No se encontraron doctores</p>
          </div>
        </div>
      ) : (
        <div className="card">
          <div style={{ overflowX: 'auto' }}>
            <table className="table table-hover">
              <thead>
                <tr>
                  <th>Nombre</th>
                  <th>Email</th>
                  <th>Licencia</th>
                  <th>Especialidad</th>
                  <th>Estado</th>
                  <th>Creado</th>
                  <th style={{ textAlign: 'right' }}>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {data.content.map(d => (
                  <tr key={d.id}>
                    <td style={{ fontWeight: 500 }}>{d.fullName}</td>
                    <td>{d.email}</td>
                    <td>{d.licenseNumber || '-'}</td>
                    <td>{d.specialtyName}</td>
                    <td>
                      <span className={`badge-status ${d.active ? 'badge-confirmed' : 'badge-cancelled'}`}>
                        {d.active ? 'Activo' : 'Inactivo'}
                      </span>
                    </td>
                    <td>{d.createdAt ? dayjs(d.createdAt).format('DD/MM/YYYY') : '-'}</td>
                    <td style={{ textAlign: 'right' }}>
                      <div className="d-flex gap-1 justify-content-end">
                        <button className="btn btn-sm btn-ghost" onClick={() => setEditor({ mode: 'edit', doctor: d })}>
                          Editar
                        </button>
                        <button
                          className="btn btn-sm btn-ghost"
                          style={{ color: d.active ? 'var(--danger)' : 'var(--success)' }}
                          onClick={() => handleToggleActive(d)}
                        >
                          {d.active ? 'Desactivar' : 'Activar'}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="d-flex justify-content-center py-3" style={{ borderTop: '1px solid var(--border)' }}>
            <Pagination page={data.number} totalPages={data.totalPages} onPage={loadDoctors} />
          </div>
        </div>
      )}
    </div>
  )
}
