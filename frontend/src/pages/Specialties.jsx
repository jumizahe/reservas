import React, { useEffect, useState, useCallback } from 'react'
import { listSpecialties, createSpecialty, updateSpecialty } from '../api/specialties'

function SpecialtyForm({ specialty, onSave, onCancel }) {
  const [form, setForm] = useState({
    name: specialty?.name || '',
    description: specialty?.description || '',
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
        <h3>{specialty ? 'Editar especialidad' : 'Nueva especialidad'}</h3>
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
            <div className="col-md-8">
              <label className="form-label">Descripción</label>
              <input className="form-control" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} />
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

export default function Specialties() {
  const [data, setData] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [editor, setEditor] = useState(null)
  const [search, setSearch] = useState('')

  const loadSpecialties = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const res = await listSpecialties()
      let specialties = res.data
      if (search.trim()) {
        specialties = specialties.filter(s =>
          s.name?.toLowerCase().includes(search.toLowerCase()) ||
          s.description?.toLowerCase().includes(search.toLowerCase())
        )
      }
      setData(specialties)
    } catch (e) {
      setError(e.message || 'Error al cargar especialidades')
    } finally {
      setLoading(false)
    }
  }, [search])

  useEffect(() => { loadSpecialties() }, [loadSpecialties])

  const handleSave = async (payload) => {
    if (editor.mode === 'create') await createSpecialty(payload)
    else await updateSpecialty(editor.specialty.id, payload)
    setEditor(null)
    await loadSpecialties()
  }

  return (
    <div>
      <div className="page-header d-flex justify-content-between align-items-start">
        <div>
          <h2>Especialidades</h2>
          <p>Gestión de especialidades médicas</p>
        </div>
        <button className="btn btn-primary" onClick={() => setEditor({ mode: 'create' })}>
          {'+ Nueva Especialidad'}
        </button>
      </div>

      {editor && (
        <SpecialtyForm
          specialty={editor.mode === 'edit' ? editor.specialty : null}
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
            placeholder="Buscar por nombre o descripción..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && loadSpecialties()}
          />
          <button className="btn btn-ghost" onClick={loadSpecialties}>Buscar</button>
        </div>
      </div>

      {loading ? (
        <div className="spinner-container"><div className="spinner" /></div>
      ) : data.length === 0 ? (
        <div className="card">
          <div className="empty-state">
            <div className="empty-icon">{'\u{1F9EC}'}</div>
            <p>No se encontraron especialidades</p>
          </div>
        </div>
      ) : (
        <div className="card">
          <div style={{ overflowX: 'auto' }}>
            <table className="table table-hover">
              <thead>
                <tr>
                  <th>Nombre</th>
                  <th>Descripción</th>
                  <th style={{ textAlign: 'right' }}>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {data.map(s => (
                  <tr key={s.id}>
                    <td style={{ fontWeight: 500 }}>{s.name}</td>
                    <td>{s.description || '-'}</td>
                    <td style={{ textAlign: 'right' }}>
                      <button className="btn btn-sm btn-ghost" onClick={() => setEditor({ mode: 'edit', specialty: s })}>
                        Editar
                      </button>
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
