import client from './client'

export const listSpecialties = () =>
  client.get('/specialties')

export const createSpecialty = (payload) =>
  client.post('/specialties', payload)

export const updateSpecialty = (id, payload) =>
  client.put(`/specialties/${id}`, payload)
