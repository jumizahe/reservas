import client from './client'

export const listOffices = () =>
  client.get('/offices')

export const createOffice = (payload) =>
  client.post('/offices', payload)

export const updateOffice = (id, payload) =>
  client.put(`/offices/${id}`, payload)
