import React, { useEffect, useState } from 'react'
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend } from 'chart.js'
import { Bar } from 'react-chartjs-2'
import dayjs from 'dayjs'

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend)

export default function Reports() {
  const [officeData, setOfficeData] = useState(null)

  useEffect(() => {
    // example: query last 30 days
    const to = dayjs().endOf('day').toISOString()
    const from = dayjs().subtract(30, 'day').startOf('day').toISOString()
    fetch(`/api/reports/office-occupancy?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`)
      .then(r => r.json())
      .then(data => {
        // expects array of { officeName, occupiedHours }
        const labels = data.map(d => d.officeName)
        const values = data.map(d => d.occupiedHours)
        setOfficeData({ labels, values })
      }).catch(() => setOfficeData(null))
  }, [])

  if (!officeData) return <div>Cargando reportes...</div>

  const chartData = {
    labels: officeData.labels,
    datasets: [{ label: 'Horas ocupadas', data: officeData.values, backgroundColor: 'rgba(13,110,253,0.6)' }]
  }

  return (
    <div>
      <h2 className="h5 mb-3">Reportes</h2>
      <div className="card p-3">
        <h6>Ocupación por oficina (últimos 30 días)</h6>
        <Bar data={chartData} />
      </div>
    </div>
  )
}
