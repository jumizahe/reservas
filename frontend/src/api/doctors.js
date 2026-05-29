import client from './client'

export const listDoctors = (page = 0, size = 10) =>
  client.get(`/doctors?page=${page}&size=${size}`)

export const listDoctorsBySpecialty = (specialtyId) =>
  client.get(`/doctors/by-specialty/${specialtyId}`)

export const getDoctor = (id) =>
  client.get(`/doctors/${id}`)

export const createDoctor = (payload) =>
  client.post('/doctors', payload)

export const updateDoctor = (id, payload) =>
  client.put(`/doctors/${id}`, payload)
