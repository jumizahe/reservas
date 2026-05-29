import React, { useState } from 'react'
import { Routes, Route, Link, useLocation } from 'react-router-dom'
import AppointmentsList from './pages/AppointmentsList'
import CreateAppointment from './pages/CreateAppointment'
import Reports from './pages/Reports'
import Doctors from './pages/Doctors'
import Patients from './pages/Patients'

const NAV_ITEMS = [
  { path: '/appointments', label: 'Citas', icon: '\u{1F4C5}' },
  { path: '/appointments/new', label: 'Nueva Cita', icon: '\u{2795}' },
  { path: '/doctors', label: 'Doctores', icon: '\u{1FA7A}' },
  { path: '/patients', label: 'Pacientes', icon: '\u{1F9D1}' },
  { path: '/reports', label: 'Reportes', icon: '\u{1F4CA}' },
]

export default function App() {
  const location = useLocation()
  const [sidebarOpen, setSidebarOpen] = useState(false)

  const isActive = (path) => {
    if (path === '/appointments') return location.pathname === '/' || location.pathname === '/appointments'
    return location.pathname === path
  }

  return (
    <>
      <button className="mobile-toggle" onClick={() => setSidebarOpen(!sidebarOpen)}>
        {'\u2630'}
      </button>

      <aside className={`app-sidebar ${sidebarOpen ? 'open' : ''}`}>
        <div className="sidebar-brand">
          <h1>Reservas</h1>
        </div>
        <nav className="sidebar-nav">
          <div className="nav-section">Principal</div>
          {NAV_ITEMS.map(item => (
            <Link
              key={item.path}
              to={item.path}
              className={`nav-link ${isActive(item.path) ? 'active' : ''}`}
              onClick={() => setSidebarOpen(false)}
            >
              <span className="nav-icon">{item.icon}</span>
              {item.label}
            </Link>
          ))}
        </nav>
      </aside>

      <main className="app-main">
        <Routes>
          <Route path="/" element={<AppointmentsList />} />
          <Route path="/appointments" element={<AppointmentsList />} />
          <Route path="/appointments/new" element={<CreateAppointment />} />
          <Route path="/doctors" element={<Doctors />} />
          <Route path="/patients" element={<Patients />} />
          <Route path="/reports" element={<Reports />} />
          <Route path="*" element={
            <div className="empty-state">
              <div className="empty-icon">{'\u{1F50D}'}</div>
              <p>Página no encontrada</p>
              <Link to="/" className="btn btn-primary mt-3">Volver al inicio</Link>
            </div>
          } />
        </Routes>
      </main>
    </>
  )
}
