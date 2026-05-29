import React, { useEffect, useState } from 'react'
import dayjs from 'dayjs'
import { listPatients, createPatient, updatePatient } from '../api/patients'

function PaginationSimple({ page, totalPages, onPage }) {
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

export default function Patients() {
  const [data, setData] = useState({ content: [], number: 0, totalPages: 1 })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [editor, setEditor] = useState(null)
  const [f, setF] = useState({ fullName: '', email: '', documentNumber: '', phone: '' })
  const [search, setSearch] = useState('')

  const fetch = async (page = 0) => {
    setLoading(true)
    setError(null)
    try {
      if (search && search.trim().length > 0) {
        const res = await listPatients(0, 500)
        const items = res.data.content.filter(p =>
          (p.fullName || '').toLowerCase().includes(search.toLowerCase()) ||
          (p.email || '').toLowerCase().includes(search.toLowerCase()) ||
          (p.documentNumber || '').toLowerCase().includes(search.toLowerCase())
        )
        setData({ content: items, number: 0, totalPages: 1 })
      } else {
        const res = await listPatients(page, 50)
        setData(res.data)
      }
    } catch (e) { setError(e.message || JSON.stringify(e)) } finally { setLoading(false) }
  }

  useEffect(() => { fetch(0) }, [])

  useEffect(() => {
    if (editor?.mode === 'edit' && editor.patient) setF({ fullName: editor.patient.fullName || '', email: editor.patient.email || '', documentNumber: editor.patient.documentNumber || '', phone: editor.patient.phone || '' })
    if (editor?.mode === 'create') setF({ fullName: '', email: '', documentNumber: '', phone: '' })
  }, [editor])

  return (
    <div>
      <h2 className="h5 mb-3">Pacientes</h2>
      <div className="mb-3 d-flex gap-2">
        <div className="flex-grow-1">
          <input className="form-control" placeholder="Buscar por nombre, email o documento" value={search} onChange={e => setSearch(e.target.value)} />
        </div>
        <div>
          <button className="btn btn-sm btn-secondary" onClick={() => fetch(0)}>Buscar</button>
        </div>
        <div>
          <button className="btn btn-sm btn-primary" onClick={() => setEditor({ mode: 'create', patient: null })}>Nuevo Paciente</button>
        </div>
      </div>

      {editor && (
        <div className="card mb-3 p-3">
          <h6>{editor.mode === 'create' ? 'Crear paciente' : 'Editar paciente'}</h6>
          <form onSubmit={async (e) => {
            e.preventDefault()
            try {
              const payload = { fullName: f.fullName, email: f.email, documentNumber: f.documentNumber, phone: f.phone }
              if (editor.mode === 'create') await createPatient(payload)
              else await updatePatient(editor.patient.id, payload)
              setEditor(null)
              fetch(data.number)
            } catch (err) { alert(err.message || JSON.stringify(err)) }
          }}>
            <div className="row">
              <div className="col-md-6 mb-2"><input className="form-control" placeholder="Nombre completo" value={f.fullName} onChange={e => setF({ ...f, fullName: e.target.value })} required /></div>
              <div className="col-md-6 mb-2"><input className="form-control" placeholder="Email" value={f.email} onChange={e => setF({ ...f, email: e.target.value })} /></div>
            </div>
            <div className="row">
              <div className="col-md-6 mb-2"><input className="form-control" placeholder="Documento" value={f.documentNumber} onChange={e => setF({ ...f, documentNumber: e.target.value })} /></div>
              <div className="col-md-6 mb-2"><input className="form-control" placeholder="Teléfono" value={f.phone} onChange={e => setF({ ...f, phone: e.target.value })} /></div>
            </div>
            <div>
              <button className="btn btn-primary btn-sm me-2" type="submit">Guardar</button>
              <button type="button" className="btn btn-secondary btn-sm" onClick={() => setEditor(null)}>Cancelar</button>
            </div>
          </form>
        </div>
      )}

      {error && <div className="alert alert-danger">{error}</div>}
      {loading ? <div>Cargando...</div> : (
        <>
          <table className="table table-sm">
            <thead>
              <tr>
                <th>ID</th>
                <th>Nombre</th>
                <th>Email</th>
                <th>Documento</th>
                <th>Activo</th>
                <th>Creado</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {data.content.map(p => (
                <tr key={p.id}>
                  <td>{p.id}</td>
                  <td>{p.fullName}</td>
                  <td>{p.email}</td>
                  <td>{p.documentNumber}</td>
                  <td>{p.active ? 'Sí' : 'No'}</td>
                  <td>{p.createdAt ? dayjs(p.createdAt).format('YYYY-MM-DD') : ''}</td>
                  <td>
                    <button className="btn btn-sm btn-outline-primary" onClick={() => setEditor({ mode: 'edit', patient: p })}>Editar</button>
                    <button className={"btn btn-sm ms-2 " + (p.active ? 'btn-outline-danger' : 'btn-outline-success')} onClick={async () => {
                      const willActivate = !p.active
                      if (!window.confirm(willActivate ? 'Activar paciente?' : 'Desactivar paciente?')) return
                      try {
                        await updatePatient(p.id, { active: willActivate })
                        fetch(data.number)
                      } catch (err) { alert(err.message || JSON.stringify(err)) }
                    }}>{p.active ? 'Desactivar' : 'Activar'}</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <PaginationSimple page={data.number} totalPages={data.totalPages} onPage={(p) => fetch(p)} />
        </>
      )}
    </div>
  )
}
