import React, { useEffect, useState, useCallback } from 'react'
import { listOffices, createOffice, updateOffice } from '../api/offices'

const STATUS_OPTIONS = [
  { value: 'AVAILABLE', label: 'Disponible', class: 'badge-confirmed' },
  { value: 'UNAVAILABLE', label: 'No disponible', class: 'badge-cancelled' },
  { value: 'MAINTENANCE', label: 'Mantenimiento', class: 'badge-noshow' },
]

function StatusBadge({ status }) {
  const info = STATUS_OPTIONS.find(s => s.value === status) || { label: status, class: '' }
  return <span className={`badge-status ${info.class}`}>{info.label}</span>
}

function OfficeForm({ office, onSave, onCancel }) {
  const [form, setForm] = useState({
    name: office?.name || '',
    location: office?.location || '',
    status: office?.status || 'AVAILABLE',
  })
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.name) {
      setError('El nombre es obligatorio')
      return
    }
    setSaving(true)
    setError(null)
    try {
      await onSave(form)
    } catch (err) {
      setError(err.message || 'Error al guardar')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="card mb-4">
      <div className="card-header-custom">
        <h3>{office ? 'Editar consultorio' : 'Nuevo consultorio'}</h3>
        <button className="btn btn-sm btn-ghost" onClick={onCancel}>Cancelar</button>
      </div>
      <div className="card-body-custom">
        {error && <div className="alert alert-danger">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="row g-3">
            <div className="col-md-4">
              <label className="form-label">Nombre *</label>
              <input className="form-control" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} required />
            </div>
            <div className="col-md-4">
              <label className="form-label">Ubicación</label>
              <input className="form-control" value={form.location} onChange={e => setForm({ ...form, location: e.target.value })} />
            </div>
            <div className="col-md-4">
              <label className="form-label">Estado</label>
              <select className="form-select" value={form.status} onChange={e => setForm({ ...form, status: e.target.value })}>
                {STATUS_OPTIONS.map(s => <option key={s.value} value={s.value}>{s.label}</option>)}
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

export default function Offices() {
  const [data, setData] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [editor, setEditor] = useState(null)
  const [search, setSearch] = useState('')

  const loadOffices = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const res = await listOffices()
      let offices = res.data
      if (search.trim()) {
        offices = offices.filter(o =>
          o.name?.toLowerCase().includes(search.toLowerCase()) ||
          o.location?.toLowerCase().includes(search.toLowerCase())
        )
      }
      setData(offices)
    } catch (e) {
      setError(e.message || 'Error al cargar consultorios')
    } finally {
      setLoading(false)
    }
  }, [search])

  useEffect(() => { loadOffices() }, [loadOffices])

  const handleSave = async (payload) => {
    if (editor.mode === 'create') await createOffice(payload)
    else await updateOffice(editor.office.id, payload)
    setEditor(null)
    await loadOffices()
  }

  return (
    <div>
      <div className="page-header d-flex justify-content-between align-items-start">
        <div>
          <h2>Consultorios</h2>
          <p>Gestión de consultorios médicos</p>
        </div>
        <button className="btn btn-primary" onClick={() => setEditor({ mode: 'create' })}>
          {'+ Nuevo Consultorio'}
        </button>
      </div>

      {editor && (
        <OfficeForm
          office={editor.mode === 'edit' ? editor.office : null}
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
            placeholder="Buscar por nombre o ubicación..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && loadOffices()}
          />
          <button className="btn btn-ghost" onClick={loadOffices}>Buscar</button>
        </div>
      </div>

      {loading ? (
        <div className="spinner-container"><div className="spinner" /></div>
      ) : data.length === 0 ? (
        <div className="card">
          <div className="empty-state">
            <div className="empty-icon">{'\u{1F3E5}'}</div>
            <p>No se encontraron consultorios</p>
          </div>
        </div>
      ) : (
        <div className="card">
          <div style={{ overflowX: 'auto' }}>
            <table className="table table-hover">
              <thead>
                <tr>
                  <th>Nombre</th>
                  <th>Ubicación</th>
                  <th>Estado</th>
                  <th style={{ textAlign: 'right' }}>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {data.map(o => (
                  <tr key={o.id}>
                    <td style={{ fontWeight: 500 }}>{o.name}</td>
                    <td>{o.location || '-'}</td>
                    <td><StatusBadge status={o.status} /></td>
                    <td style={{ textAlign: 'right' }}>
                      <div className="d-flex gap-1 justify-content-end align-items-center">
                        <button className="btn btn-sm btn-ghost" onClick={() => setEditor({ mode: 'edit', office: o })}>
                          Editar
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}
