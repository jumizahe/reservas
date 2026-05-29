import client from './client'

export const listPatients = (page = 0, size = 50) =>
  client.get(`/patients?page=${page}&size=${size}`)

export const createPatient = (payload) =>
  client.post('/patients', payload)

export const getPatient = (id) =>
  client.get(`/patients/${id}`)

export const updatePatient = (id, payload) =>
  client.put(`/patients/${id}`, payload)
