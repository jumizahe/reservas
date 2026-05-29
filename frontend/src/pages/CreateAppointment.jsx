import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import dayjs from 'dayjs'
import { listPatients } from '../api/patients'
import { listDoctorsBySpecialty } from '../api/doctors'
import { getAvailability } from '../api/availability'
import { createAppointment } from '../api/appointments'
import AvailabilitySlots from '../components/AvailabilitySlots'
import client from '../api/client'

export default function CreateAppointment() {
  const navigate = useNavigate()
  const [patients, setPatients] = useState([])
  const [specialties, setSpecialties] = useState([])
  const [appointmentTypes, setAppointmentTypes] = useState([])
  const [offices, setOffices] = useState([])
  const [doctors, setDoctors] = useState([])
  const [slots, setSlots] = useState([])
  const [loading, setLoading] = useState(false)
  const [loadingSlots, setLoadingSlots] = useState(false)
  const [error, setError] = useState(null)
  const [form, setForm] = useState({
    patientId: '', specialtyId: '', doctorId: '', officeId: '',
    appointmentTypeId: '', date: '', selectedSlot: null,
  })

  useEffect(() => {
    Promise.all([
      listPatients(0, 100).then(r => setPatients(r.data.content)),
      client.get('/specialties').then(r => setSpecialties(r.data)),
      client.get('/appointment-types').then(r => setAppointmentTypes(r.data)),
      client.get('/offices').then(r => setOffices(r.data)),
    ]).catch(() => setError('Error al cargar datos del formulario'))
  }, [])

  useEffect(() => {
    if (form.specialtyId) {
      listDoctorsBySpecialty(form.specialtyId).then(r => setDoctors(r.data)).catch(() => setDoctors([]))
      setForm(f => ({ ...f, doctorId: '', selectedSlot: null }))
      setSlots([])
    }
  }, [form.specialtyId])

  useEffect(() => {
    if (form.doctorId && form.date) {
      setLoadingSlots(true)
      const dateStr = dayjs(form.date).format('YYYY-MM-DD')
      getAvailability(form.doctorId, dateStr, 30)
        .then(r => setSlots(r.data))
        .catch(() => setSlots([]))
        .finally(() => setLoadingSlots(false))
    }
  }, [form.doctorId, form.date])

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.selectedSlot) { setError('Selecciona un horario disponible'); return }
    if (!form.patientId || !form.doctorId || !form.officeId || !form.appointmentTypeId) {
      setError('Completa todos los campos obligatorios'); return
    }
    setLoading(true)
    setError(null)
    try {
      await createAppointment({
        patientId: Number(form.patientId),
        doctorId: Number(form.doctorId),
        officeId: Number(form.officeId),
        appointmentTypeId: Number(form.appointmentTypeId),
        startAt: dayjs(form.selectedSlot.startAt).format('YYYY-MM-DDTHH:mm:ss'),
      })
      navigate('/appointments')
    } catch (e) {
      setError(e.message || 'Error al crear la cita')
    } finally {
      setLoading(false)
    }
  }

  const update = (field, value) => setForm(f => ({ ...f, [field]: value }))

  return (
    <div>
      <div className="page-header">
        <h2>Nueva Cita</h2>
        <p>Agendar una nueva cita médica</p>
      </div>

      {error && (
        <div className="alert alert-danger d-flex align-items-center justify-content-between mb-3">
          <span>{error}</span>
          <button className="btn btn-sm btn-ghost" onClick={() => setError(null)}>Cerrar</button>
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="card mb-4">
          <div className="card-header-custom">
            <h3>Datos de la cita</h3>
          </div>
          <div className="card-body-custom">
            <div className="row g-3">
              <div className="col-md-6">
                <label className="form-label">Paciente *</label>
                <select className="form-select" value={form.patientId} onChange={e => update('patientId', e.target.value)} required>
                  <option value="">Seleccionar paciente</option>
                  {patients.map(p => <option key={p.id} value={p.id}>{p.fullName} ({p.documentNumber})</option>)}
                </select>
              </div>
              <div className="col-md-6">
                <label className="form-label">Tipo de cita *</label>
                <select className="form-select" value={form.appointmentTypeId} onChange={e => update('appointmentTypeId', e.target.value)} required>
                  <option value="">Seleccionar tipo</option>
                  {appointmentTypes.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
                </select>
              </div>
              <div className="col-md-4">
                <label className="form-label">Especialidad *</label>
                <select className="form-select" value={form.specialtyId} onChange={e => update('specialtyId', e.target.value)} required>
                  <option value="">Seleccionar especialidad</option>
                  {specialties.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
                </select>
              </div>
              <div className="col-md-4">
                <label className="form-label">Doctor *</label>
                <select className="form-select" value={form.doctorId} onChange={e => update('doctorId', e.target.value)} required disabled={!form.specialtyId}>
                  <option value="">{form.specialtyId ? 'Seleccionar doctor' : 'Primero seleccione especialidad'}</option>
                  {doctors.map(d => <option key={d.id} value={d.id}>{d.fullName}</option>)}
                </select>
              </div>
              <div className="col-md-4">
                <label className="form-label">Consultorio *</label>
                <select className="form-select" value={form.officeId} onChange={e => update('officeId', e.target.value)} required>
                  <option value="">Seleccionar consultorio</option>
                  {offices.map(o => <option key={o.id} value={o.id}>{o.name}</option>)}
                </select>
              </div>
              <div className="col-md-4">
                <label className="form-label">Fecha *</label>
                <input
                  type="date"
                  className="form-control"
                  value={form.date}
                  min={dayjs().format('YYYY-MM-DD')}
                  onChange={e => { update('date', e.target.value); update('selectedSlot', null) }}
                  required
                />
              </div>
            </div>
          </div>
        </div>

        {form.doctorId && form.date && (
          <div className="card mb-4">
            <div className="card-header-custom">
              <h3>Horarios disponibles</h3>
              {loadingSlots && <span className="text-muted" style={{ fontSize: '0.8125rem' }}>Cargando...</span>}
            </div>
            <div className="card-body-custom">
              <AvailabilitySlots
                slots={slots}
                selected={form.selectedSlot}
                onSelect={(s) => update('selectedSlot', s)}
              />
            </div>
          </div>
        )}

        <div className="d-flex gap-2">
          <button className="btn btn-primary" type="submit" disabled={loading}>
            {loading ? 'Agendando...' : 'Agendar Cita'}
          </button>
          <button className="btn btn-ghost" type="button" onClick={() => navigate('/appointments')}>Cancelar</button>
        </div>
      </form>
    </div>
  )
}
