import client from './client'

export const listAppointments = (page = 0, size = 10) =>
  client.get(`/appointments?page=${page}&size=${size}`)

export const createAppointment = (payload) =>
  client.post('/appointments', payload)

export const getAppointment = (id) =>
  client.get(`/appointments/${id}`)

export const confirmAppointment = (id) =>
  client.put(`/appointments/${id}/confirm`)

export const cancelAppointment = (id, reason) =>
  client.put(`/appointments/${id}/cancel`, { reason })

export const completeAppointment = (id, observations) =>
  client.put(`/appointments/${id}/complete`, null, { params: { observations } })

export const markNoShow = (id) =>
  client.put(`/appointments/${id}/no-show`)
