import client from './client'

export const getAvailability = (doctorId, date, durationMinutes = 30) =>
  client.get(`/availability/doctors/${doctorId}`, { params: { date, durationMinutes } })
