import React from 'react'

export default function Pagination({ page, totalPages, onPage }) {
  return (
    <nav>
      <ul className="pagination">
        <li className={`page-item ${page <= 0 ? 'disabled' : ''}`}>
          <button className="page-link" onClick={() => onPage(page - 1)}>Anterior</button>
        </li>
        <li className="page-item disabled"><span className="page-link">{page + 1} / {totalPages}</span></li>
        <li className={`page-item ${page + 1 >= totalPages ? 'disabled' : ''}`}>
          <button className="page-link" onClick={() => onPage(page + 1)}>Siguiente</button>
        </li>
      </ul>
    </nav>
  )
}
