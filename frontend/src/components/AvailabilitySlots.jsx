import React from 'react'
import dayjs from 'dayjs'

export default function AvailabilitySlots({ slots = [], selected, onSelect }) {
  if (!slots || slots.length === 0) return <div className="alert alert-info">No hay slots disponibles para la fecha seleccionada.</div>

  return (
    <div>
      <h6>Slots disponibles</h6>
      <div className="d-flex flex-wrap gap-2">
        {slots.map((s, idx) => {
          const start = dayjs(s.startAt).format('HH:mm')
          const end = dayjs(s.endAt).format('HH:mm')
          const isSelected = selected && selected.startAt === s.startAt
          return (
            <button key={idx} type="button" className={`btn ${isSelected ? 'btn-primary' : 'btn-outline-secondary'}`} onClick={() => onSelect(s)}>
              {start} - {end}
            </button>
          )
        })}
      </div>
    </div>
  )
}
