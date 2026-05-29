import React from 'react'

export default function Pagination({ page, totalPages, onPage }) {
  if (totalPages <= 1) return null

  return (
    <nav aria-label="Paginación">
      <ul className="pagination mb-0">
        <li className={`page-item ${page <= 0 ? 'disabled' : ''}`}>
          <button className="page-link" onClick={() => onPage(page - 1)} aria-label="Anterior">
            &lsaquo; Anterior
          </button>
        </li>
        <li className="page-item disabled">
          <span className="page-link">{page + 1} / {totalPages}</span>
        </li>
        <li className={`page-item ${page + 1 >= totalPages ? 'disabled' : ''}`}>
          <button className="page-link" onClick={() => onPage(page + 1)} aria-label="Siguiente">
            Siguiente &rsaquo;
          </button>
        </li>
      </ul>
    </nav>
  )
}
