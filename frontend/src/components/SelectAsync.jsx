import React from 'react'

// Minimal select component, expects items array with id and label
export default function SelectAsync({ items = [], value, onChange, placeholder = '-- seleccionar --' }) {
  return (
    <select className="form-select" value={value} onChange={e => onChange(e.target.value)}>
      <option value="">{placeholder}</option>
      {items.map(i => <option key={i.id} value={i.id}>{i.label}</option>)}
    </select>
  )
}
