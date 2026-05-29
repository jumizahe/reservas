import React, { useEffect, useState, useCallback, useMemo } from 'react'
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend } from 'chart.js'
import { Bar } from 'react-chartjs-2'
import dayjs from 'dayjs'
import { getDoctorProductivity, getOfficeOccupancy, getNoShowPatients, getSpecialties } from '../api/reports'

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend)

const TABS = [
  { key: 'doctors', label: 'Doctores', icon: '\u{1FA7A}' },
  { key: 'offices', label: 'Consultorios', icon: '\u{1F3E5}' },
  { key: 'noshows', label: 'No-Shows', icon: '\u{26A0}' },
]

const CHART_COLORS = [
  '#4f46e5', '#059669', '#d97706', '#dc2626', '#2563eb',
  '#7c3aed', '#db2777', '#0891b2', '#65a30d', '#e11d48',
]

const CHART_OPTIONS = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      backgroundColor: '#1f2937',
      titleFont: { size: 13, weight: '600' },
      bodyFont: { size: 12 },
      padding: 10,
      cornerRadius: 8,
    },
  },
  scales: {
    y: { beginAtZero: true, ticks: { stepSize: 1, font: { size: 12 } }, grid: { color: '#f3f4f6' } },
    x: { ticks: { font: { size: 11 } }, grid: { display: false } },
  },
}

function LoadingSpinner() {
  return (
    <div className="spinner-container">
      <div className="spinner" />
    </div>
  )
}

function EmptyState({ message }) {
  return (
    <div className="empty-state">
      <div className="empty-icon">{'\u{1F4CB}'}</div>
      <p>{message}</p>
    </div>
  )
}

function StatCard({ label, value, color = 'var(--primary)' }) {
  return (
    <div className="stat-card">
      <div className="stat-label">{label}</div>
      <div className="stat-value" style={{ color }}>{value}</div>
    </div>
  )
}

function ReportChart({ data, labelKey, valueKey, label }) {
  const chartData = {
    labels: data.map(d => d[labelKey]),
    datasets: [{
      label,
      data: data.map(d => d[valueKey]),
      backgroundColor: data.map((_, i) => CHART_COLORS[i % CHART_COLORS.length]),
      borderRadius: 6,
      borderSkipped: false,
    }],
  }

  return (
    <div className="chart-container">
      <Bar data={chartData} options={CHART_OPTIONS} />
    </div>
  )
}

function DataTable({ columns, data, emptyMessage = 'No hay datos' }) {
  if (!data || data.length === 0) return <EmptyState message={emptyMessage} />
  return (
    <div style={{ overflowX: 'auto' }}>
      <table className="table table-hover">
        <thead>
          <tr>
            {columns.map(col => (
              <th key={col.key} style={col.align === 'right' ? { textAlign: 'right' } : {}}>
                {col.label}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data.map((row, i) => (
            <tr key={row.id || i}>
              {columns.map(col => (
                <td key={col.key} style={col.align === 'right' ? { textAlign: 'right' } : {}}>
                  {col.render ? col.render(row[col.key], row) : row[col.key]}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default function Reports() {
  const [activeTab, setActiveTab] = useState('doctors')
  const [specialties, setSpecialties] = useState([])
  const [specialtyId, setSpecialtyId] = useState('')
  const [dateFrom, setDateFrom] = useState(dayjs().subtract(30, 'day').format('YYYY-MM-DD'))
  const [dateTo, setDateTo] = useState(dayjs().format('YYYY-MM-DD'))
  const [doctorData, setDoctorData] = useState([])
  const [officeData, setOfficeData] = useState([])
  const [noShowData, setNoShowData] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    getSpecialties().then(r => setSpecialties(r.data)).catch(() => {})
  }, [])

  const fetchReports = useCallback(() => {
    setLoading(true)
    setError(null)
    const from = dayjs(dateFrom).startOf('day').toISOString()
    const to = dayjs(dateTo).endOf('day').toISOString()
    const specId = specialtyId || null

    Promise.all([
      getDoctorProductivity(from, to, specId),
      getOfficeOccupancy(from, to, specId),
      getNoShowPatients(from, to, 10),
    ])
      .then(([doctors, offices, noShow]) => {
        setDoctorData(doctors.data)
        setOfficeData(offices.data)
        setNoShowData(noShow.data)
      })
      .catch(() => setError('Error al cargar los reportes. Verifica la conexión con el servidor.'))
      .finally(() => setLoading(false))
  }, [dateFrom, dateTo, specialtyId])

  useEffect(() => { fetchReports() }, [fetchReports])

  const totalDoctors = useMemo(() => doctorData.length, [doctorData])
  const totalOffices = useMemo(() => officeData.length, [officeData])
  const totalCompleted = useMemo(() => doctorData.reduce((s, d) => s + d.completedAppointments, 0), [doctorData])
  const totalNoShows = useMemo(() => noShowData.reduce((s, d) => s + d.noShowCount, 0), [noShowData])

  const doctorColumns = [
    { key: 'doctorName', label: 'Doctor' },
    { key: 'specialtyName', label: 'Especialidad' },
    { key: 'completedAppointments', label: 'Citas', align: 'right' },
  ]

  const officeColumns = [
    { key: 'officeName', label: 'Consultorio' },
    { key: 'totalAppointments', label: 'Total Citas', align: 'right' },
  ]

  const noShowColumns = [
    { key: 'fullName', label: 'Paciente' },
    { key: 'documentNumber', label: 'Documento' },
    { key: 'noShowCount', label: 'Inasistencias', align: 'right' },
  ]

  return (
    <div>
      <div className="page-header">
        <h2>Reportes</h2>
        <p>Análisis de productividad, ocupación e inasistencias</p>
      </div>

      <div className="filters-bar">
        <div className="row g-3 align-items-end">
          <div className="col-md-3">
            <label className="form-label">Especialidad</label>
            <select className="form-select" value={specialtyId} onChange={e => setSpecialtyId(e.target.value)}>
              <option value="">Todas las especialidades</option>
              {specialties.map(s => (
                <option key={s.id} value={s.id}>{s.name}</option>
              ))}
            </select>
          </div>
          <div className="col-md-3">
            <label className="form-label">Desde</label>
            <input type="date" className="form-control" value={dateFrom} onChange={e => setDateFrom(e.target.value)} />
          </div>
          <div className="col-md-3">
            <label className="form-label">Hasta</label>
            <input type="date" className="form-control" value={dateTo} onChange={e => setDateTo(e.target.value)} />
          </div>
          <div className="col-md-3">
            <button className="btn btn-primary w-100" onClick={fetchReports} disabled={loading}>
              {loading ? 'Actualizando...' : 'Actualizar'}
            </button>
          </div>
        </div>
      </div>

      <div className="row g-3 mb-4">
        <div className="col-md-3">
          <StatCard label="Doctores activos" value={totalDoctors} color="var(--primary)" />
        </div>
        <div className="col-md-3">
          <StatCard label="Consultorios" value={totalOffices} color="var(--success)" />
        </div>
        <div className="col-md-3">
          <StatCard label="Citas completadas" value={totalCompleted} color="var(--info)" />
        </div>
        <div className="col-md-3">
          <StatCard label="Total inasistencias" value={totalNoShows} color="var(--danger)" />
        </div>
      </div>

      <div className="d-flex gap-2 mb-4">
        {TABS.map(tab => (
          <button
            key={tab.key}
            className={`nav-tab ${activeTab === tab.key ? 'active' : ''}`}
            onClick={() => setActiveTab(tab.key)}
          >
            <span style={{ marginRight: 6 }}>{tab.icon}</span>
            {tab.label}
          </button>
        ))}
      </div>

      {error && <div className="alert alert-danger mb-3">{error}</div>}

      {loading ? <LoadingSpinner /> : (
        <>
          {activeTab === 'doctors' && (
            <div className="card">
              <div className="card-header-custom">
                <h3>Productividad por doctor</h3>
                <span className="badge-status badge-scheduled">{totalCompleted} citas completadas</span>
              </div>
              <div className="card-body-custom">
                {doctorData.length > 0 ? (
                  <>
                    <ReportChart data={doctorData} labelKey="doctorName" valueKey="completedAppointments" label="Citas completadas" />
                    <div className="mt-3">
                      <DataTable columns={doctorColumns} data={doctorData} emptyMessage="No hay datos de productividad" />
                    </div>
                  </>
                ) : <EmptyState message="No hay datos de productividad para el período seleccionado" />}
              </div>
            </div>
          )}

          {activeTab === 'offices' && (
            <div className="card">
              <div className="card-header-custom">
                <h3>Ocupación por consultorio</h3>
                <span className="badge-status badge-confirmed">{totalOffices} consultorios</span>
              </div>
              <div className="card-body-custom">
                {officeData.length > 0 ? (
                  <>
                    <ReportChart data={officeData} labelKey="officeName" valueKey="totalAppointments" label="Total citas" />
                    <div className="mt-3">
                      <DataTable columns={officeColumns} data={officeData} emptyMessage="No hay datos de ocupación" />
                    </div>
                  </>
                ) : <EmptyState message="No hay datos de ocupación para el período seleccionado" />}
              </div>
            </div>
          )}

          {activeTab === 'noshows' && (
            <div className="card">
              <div className="card-header-custom">
                <h3>Pacientes con más inasistencias</h3>
                <span className="badge-status badge-noshow">{totalNoShows} no-shows</span>
              </div>
              <div className="card-body-custom">
                {noShowData.length > 0 ? (
                  <>
                    <ReportChart data={noShowData} labelKey="fullName" valueKey="noShowCount" label="Inasistencias" />
                    <div className="mt-3">
                      <DataTable columns={noShowColumns} data={noShowData} emptyMessage="No hay inasistencias registradas" />
                    </div>
                  </>
                ) : <EmptyState message="No hay inasistencias para el período seleccionado" />}
              </div>
            </div>
          )}
        </>
      )}
    </div>
  )
}
