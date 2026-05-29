import React from 'react'
import dayjs from 'dayjs'

export default function AvailabilitySlots({ slots = [], selected, onSelect }) {
  if (!slots || slots.length === 0) {
    return (
      <div className="empty-state" style={{ padding: '24px' }}>
        <div className="empty-icon">{'\u{1F552}'}</div>
        <p>Selecciona un doctor y fecha para ver los horarios disponibles</p>
      </div>
    )
  }

  return (
    <div className="d-flex flex-wrap gap-2">
      {slots.map((s, idx) => {
        const start = dayjs(s.startAt).format('HH:mm')
        const end = dayjs(s.endAt).format('HH:mm')
        const isSelected = selected?.startAt === s.startAt
        return (
          <button
            key={`${start}-${idx}`}
            type="button"
            className={`btn ${isSelected ? 'btn-primary' : 'btn-ghost'}`}
            style={isSelected ? {} : { minWidth: 110 }}
            onClick={() => onSelect(s)}
          >
            {start} - {end}
          </button>
        )
      })}
    </div>
  )
}
