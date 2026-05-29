import React, { useEffect, useState, useCallback } from 'react'
import { listPatients, createPatient, updatePatient } from '../api/patients'
import Pagination from '../components/Pagination'
import dayjs from 'dayjs'

function PatientForm({ patient, onSave, onCancel }) {
  const [form, setForm] = useState({
    fullName: patient?.fullName || '',
    email: patient?.email || '',
    documentNumber: patient?.documentNumber || '',
    phone: patient?.phone || '',
  })
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.fullName) {
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
        <h3>{patient ? 'Editar paciente' : 'Nuevo paciente'}</h3>
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
              <label className="form-label">Email</label>
              <input className="form-control" type="email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} />
            </div>
            <div className="col-md-6">
              <label className="form-label">Documento</label>
              <input className="form-control" value={form.documentNumber} onChange={e => setForm({ ...form, documentNumber: e.target.value })} />
            </div>
            <div className="col-md-6">
              <label className="form-label">Teléfono</label>
              <input className="form-control" value={form.phone} onChange={e => setForm({ ...form, phone: e.target.value })} />
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

export default function Patients() {
  const [data, setData] = useState({ content: [], number: 0, totalPages: 1 })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [editor, setEditor] = useState(null)
  const [search, setSearch] = useState('')

  const loadPatients = useCallback(async (page = 0) => {
    setLoading(true)
    setError(null)
    try {
      if (search.trim()) {
        const res = await listPatients(0, 200)
        const filtered = res.data.content.filter(p =>
          p.fullName?.toLowerCase().includes(search.toLowerCase()) ||
          p.email?.toLowerCase().includes(search.toLowerCase()) ||
          p.documentNumber?.toLowerCase().includes(search.toLowerCase())
        )
        setData({ content: filtered, number: 0, totalPages: 1 })
      } else {
        const res = await listPatients(page, 10)
        setData(res.data)
      }
    } catch (e) {
      setError(e.message || 'Error al cargar pacientes')
    } finally {
      setLoading(false)
    }
  }, [search])

  useEffect(() => { loadPatients(0) }, [loadPatients])

  const handleSave = async (payload) => {
    if (editor.mode === 'create') await createPatient(payload)
    else await updatePatient(editor.patient.id, payload)
    setEditor(null)
    await loadPatients(data.number)
  }

  return (
    <div>
      <div className="page-header d-flex justify-content-between align-items-start">
        <div>
          <h2>Pacientes</h2>
          <p>Gestión de pacientes registrados</p>
        </div>
        <button className="btn btn-primary" onClick={() => setEditor({ mode: 'create' })}>
          {'+ Nuevo Paciente'}
        </button>
      </div>

      {editor && (
        <PatientForm
          patient={editor.mode === 'edit' ? editor.patient : null}
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
            placeholder="Buscar por nombre, email o documento..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && loadPatients(0)}
          />
          <button className="btn btn-ghost" onClick={() => loadPatients(0)}>Buscar</button>
        </div>
      </div>

      {loading ? (
        <div className="spinner-container"><div className="spinner" /></div>
      ) : data.content.length === 0 ? (
        <div className="card">
          <div className="empty-state">
            <div className="empty-icon">{'\u{1F9D1}'}</div>
            <p>No se encontraron pacientes</p>
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
                  <th>Documento</th>
                  <th>Teléfono</th>
                  <th>Creado</th>
                  <th style={{ textAlign: 'right' }}>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {data.content.map(p => (
                  <tr key={p.id}>
                    <td style={{ fontWeight: 500 }}>{p.fullName}</td>
                    <td>{p.email || '-'}</td>
                    <td>{p.documentNumber || '-'}</td>
                    <td>{p.phone || '-'}</td>
                    <td>{p.createdAt ? dayjs(p.createdAt).format('DD/MM/YYYY') : '-'}</td>
                    <td style={{ textAlign: 'right' }}>
                      <div className="d-flex gap-1 justify-content-end">
                        <button className="btn btn-sm btn-ghost" onClick={() => setEditor({ mode: 'edit', patient: p })}>
                          Editar
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="d-flex justify-content-center py-3" style={{ borderTop: '1px solid var(--border)' }}>
            <Pagination page={data.number} totalPages={data.totalPages} onPage={loadPatients} />
          </div>
        </div>
      )}
    </div>
  )
}
