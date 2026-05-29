import React from 'react'
import { Routes, Route, Link } from 'react-router-dom'
import AppointmentsList from './pages/AppointmentsList'
import CreateAppointment from './pages/CreateAppointment'
import Reports from './pages/Reports'
import Doctors from './pages/Doctors'
import Patients from './pages/Patients'

export default function App() {
  return (
    <div className="container py-4">
      <header className="d-flex justify-content-between align-items-center mb-4">
        <h1 className="h4">Reservas</h1>
        <nav>
          <Link to="/appointments" className="btn btn-outline-primary me-2">Citas</Link>
          <Link to="/appointments/new" className="btn btn-primary">Nueva cita</Link>
        </nav>
      </header>

      <div className="row">
        <div className="col-md-3 mb-3">
          <div className="list-group">
            <Link to="/appointments" className="list-group-item list-group-item-action">Citas</Link>
            <Link to="/appointments/new" className="list-group-item list-group-item-action">Nueva cita</Link>
            <Link to="/doctors" className="list-group-item list-group-item-action">Doctores</Link>
            <Link to="/patients" className="list-group-item list-group-item-action">Pacientes</Link>
            <Link to="/reports" className="list-group-item list-group-item-action">Reportes</Link>
          </div>
        </div>
        <div className="col-md-9">
          <Routes>
            <Route path="/" element={<AppointmentsList />} />
            <Route path="/appointments" element={<AppointmentsList />} />
            <Route path="/appointments/new" element={<CreateAppointment />} />
            <Route path="/doctors" element={<Doctors />} />
            <Route path="/patients" element={<Patients />} />
            <Route path="/reports" element={<Reports />} />
          </Routes>
        </div>
      </div>
    </div>
  )
}
