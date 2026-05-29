import React, { useEffect, useState } from 'react'
import { listDoctors, createDoctor, updateDoctor } from '../api/doctors'
import dayjs from 'dayjs'

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

export default function Doctors() {
  const [data, setData] = useState({ content: [], number: 0, totalPages: 1 })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [editor, setEditor] = useState(null)
  const [f, setF] = useState({ fullName: '', email: '', licenseNumber: '', specialtyId: '' })
  const [search, setSearch] = useState('')

  const fetch = async (page = 0) => {
    setLoading(true)
    setError(null)
    try {
      // If there's a search term, fetch a larger page and filter client-side
      if (search && search.trim().length > 0) {
        const res = await listDoctors(0, 100)
        const items = res.data.content.filter(d =>
          (d.fullName || '').toLowerCase().includes(search.toLowerCase()) ||
          (d.email || '').toLowerCase().includes(search.toLowerCase())
        )
        setData({ content: items, number: 0, totalPages: 1 })
      } else {
        const res = await listDoctors(page, 10)
        setData(res.data)
      }
    } catch (e) {
      setError(e.message || JSON.stringify(e))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetch(0) }, [])

  useEffect(() => {
    if (editor?.mode === 'edit' && editor.doctor) {
      const d = editor.doctor
      setF({ fullName: d.fullName || '', email: d.email || '', licenseNumber: d.licenseNumber || '', specialtyId: d.specialtyId || '' })
    }
    if (editor?.mode === 'create') setF({ fullName: '', email: '', licenseNumber: '', specialtyId: '' })
  }, [editor])

  return (
    <div>
      <h2 className="h5 mb-3">Doctores</h2>
      <div className="mb-3 d-flex gap-2">
        <div className="flex-grow-1">
          <input className="form-control" placeholder="Buscar por nombre o email" value={search} onChange={e => setSearch(e.target.value)} />
        </div>
        <div>
          <button className="btn btn-sm btn-secondary" onClick={() => fetch(0)}>Buscar</button>
        </div>
        <div>
          <button className="btn btn-sm btn-primary" onClick={() => setEditor({ mode: 'create', doctor: null })}>Nuevo Doctor</button>
        </div>
      </div>
      {editor && (
        <div className="card mb-3 p-3">
          <h6>{editor.mode === 'create' ? 'Crear doctor' : 'Editar doctor'}</h6>
          <form onSubmit={async (e) => {
            e.preventDefault()
            try {
              const payload = { fullName: f.fullName, email: f.email, licenseNumber: f.licenseNumber, specialtyId: Number(f.specialtyId) }
              if (editor.mode === 'create') await createDoctor(payload)
              else await updateDoctor(editor.doctor.id, payload)
              setEditor(null)
              fetch(data.number)
            } catch (err) { alert(err.message || JSON.stringify(err)) }
          }}>
            <div className="row">
              <div className="col-md-6 mb-2"><input className="form-control" placeholder="Nombre completo" value={f.fullName} onChange={e => setF({ ...f, fullName: e.target.value })} required /></div>
              <div className="col-md-6 mb-2"><input className="form-control" placeholder="Email" value={f.email} onChange={e => setF({ ...f, email: e.target.value })} required /></div>
            </div>
            <div className="row">
              <div className="col-md-6 mb-2"><input className="form-control" placeholder="Número de licencia" value={f.licenseNumber} onChange={e => setF({ ...f, licenseNumber: e.target.value })} /></div>
              <div className="col-md-6 mb-2"><input className="form-control" placeholder="Especialidad (id)" value={f.specialtyId} onChange={e => setF({ ...f, specialtyId: e.target.value })} required /></div>
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
                <th>Especialidad</th>
                <th>Activo</th>
                <th>Creado</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {data.content.map(d => (
                <tr key={d.id}>
                  <td>{d.id}</td>
                  <td>{d.fullName}</td>
                  <td>{d.email}</td>
                  <td>{d.specialtyName}</td>
                  <td>{d.active ? 'Sí' : 'No'}</td>
                  <td>{d.createdAt ? dayjs(d.createdAt).format('YYYY-MM-DD') : ''}</td>
                  <td>
                    <button className="btn btn-sm btn-outline-primary" onClick={() => setEditor({ mode: 'edit', doctor: d })}>Editar</button>
                    <button className={"btn btn-sm ms-2 " + (d.active ? 'btn-outline-danger' : 'btn-outline-success')} onClick={async () => {
                      const willActivate = !d.active
                      if (!window.confirm(willActivate ? 'Activar doctor?' : 'Desactivar doctor?')) return
                      try {
                        await updateDoctor(d.id, { active: willActivate })
                        fetch(data.number)
                      } catch (err) { alert(err.message || JSON.stringify(err)) }
                    }}>{d.active ? 'Desactivar' : 'Activar'}</button>
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
