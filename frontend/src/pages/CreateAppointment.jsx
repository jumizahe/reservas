import React, { useEffect, useState } from 'react'
import dayjs from 'dayjs'
import { listPatients } from '../api/patients'
import { listDoctorsBySpecialty } from '../api/doctors'
import { getAvailability } from '../api/availability'
import { createAppointment } from '../api/appointments'
import AvailabilitySlots from '../components/AvailabilitySlots'

export default function CreateAppointment() {
  const [patients, setPatients] = useState([])
  const [specialties, setSpecialties] = useState([])
  const [appointmentTypes, setAppointmentTypes] = useState([])
  const [offices, setOffices] = useState([])

  const [form, setForm] = useState({ patientId: '', specialtyId: '', doctorId: '', officeId: '', appointmentTypeId: '', date: '', selectedSlot: null })
  const [doctors, setDoctors] = useState([])
  const [slots, setSlots] = useState([])

  useEffect(() => {
    // load simple lists
    listPatients(0, 50).then(r => setPatients(r.data.content)).catch(() => {})
    fetch('/api/specialties').then(r => r.json()).then(r => setSpecialties(r)).catch(() => {})
    fetch('/api/appointment-types').then(r => r.json()).then(r => setAppointmentTypes(r)).catch(() => {})
    fetch('/api/offices').then(r => r.json()).then(r => setOffices(r)).catch(() => {})
  }, [])

  useEffect(() => {
    if (form.specialtyId) {
      listDoctorsBySpecialty(form.specialtyId).then(r => setDoctors(r.data)).catch(() => setDoctors([]))
    }
  }, [form.specialtyId])

  useEffect(() => {
    if (form.doctorId && form.date) {
      const dateStr = dayjs(form.date).format('YYYY-MM-DD')
      getAvailability(form.doctorId, dateStr, 30).then(r => setSlots(r.data)).catch(() => setSlots([]))
    }
  }, [form.doctorId, form.date])

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.selectedSlot) { alert('Elige un slot de disponibilidad'); return }
    const payload = {
      patientId: Number(form.patientId),
      doctorId: Number(form.doctorId),
      officeId: Number(form.officeId),
      appointmentTypeId: Number(form.appointmentTypeId),
      startAt: dayjs(form.selectedSlot.startAt).format('YYYY-MM-DDTHH:mm:ss')
    }
    try {
      await createAppointment(payload)
      alert('Cita creada')
      // reset
      setForm({ patientId: '', specialtyId: '', doctorId: '', officeId: '', appointmentTypeId: '', date: '', selectedSlot: null })
      setSlots([])
    } catch (e) {
      alert(e.message || JSON.stringify(e))
    }
  }

  return (
    <div>
      <h2 className="h5 mb-3">Nueva cita</h2>
      <form onSubmit={handleSubmit}>
        <div className="mb-3">
          <label className="form-label">Paciente</label>
          <select className="form-select" value={form.patientId} onChange={e => setForm({ ...form, patientId: e.target.value })}>
            <option value="">-- seleccionar paciente --</option>
            {patients.map(p => <option key={p.id} value={p.id}>{p.fullName} ({p.documentNumber})</option>)}
          </select>
        </div>

        <div className="row">
          <div className="col-md-4 mb-3">
            <label className="form-label">Especialidad</label>
            <select className="form-select" value={form.specialtyId} onChange={e => setForm({ ...form, specialtyId: e.target.value })}>
              <option value="">-- seleccionar --</option>
              {specialties.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
            </select>
          </div>
          <div className="col-md-4 mb-3">
            <label className="form-label">Doctor</label>
            <select className="form-select" value={form.doctorId} onChange={e => setForm({ ...form, doctorId: e.target.value })}>
              <option value="">-- seleccionar --</option>
              {doctors.map(d => <option key={d.id} value={d.id}>{d.fullName}</option>)}
            </select>
          </div>
          <div className="col-md-4 mb-3">
            <label className="form-label">Oficina</label>
            <select className="form-select" value={form.officeId} onChange={e => setForm({ ...form, officeId: e.target.value })}>
              <option value="">-- seleccionar --</option>
              {offices.map(o => <option key={o.id} value={o.id}>{o.name}</option>)}
            </select>
          </div>
        </div>

        <div className="mb-3">
          <label className="form-label">Tipo de cita</label>
          <select className="form-select" value={form.appointmentTypeId} onChange={e => setForm({ ...form, appointmentTypeId: e.target.value })}>
            <option value="">-- seleccionar --</option>
            {appointmentTypes.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
          </select>
        </div>

        <div className="mb-3">
          <label className="form-label">Fecha</label>
          <input className="form-control" type="date" value={form.date} onChange={e => setForm({ ...form, date: e.target.value })} />
        </div>

        <AvailabilitySlots slots={slots} selected={form.selectedSlot} onSelect={(s) => setForm({ ...form, selectedSlot: s })} />

        <div className="mt-3">
          <button className="btn btn-primary" type="submit">Reservar</button>
        </div>
      </form>
    </div>
  )
}
