import axios from 'axios'

const BASE = import.meta.env.VITE_API_BASE || '/api'

const client = axios.create({
  baseURL: BASE,
  headers: { 'Content-Type': 'application/json' }
})

client.interceptors.response.use(
  res => res,
  err => {
    // Normalize error
    if (err.response && err.response.data) {
      return Promise.reject(err.response.data)
    }
    return Promise.reject({ message: err.message })
  }
)

export default client
