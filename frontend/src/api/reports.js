import client from './client'

export const getDoctorProductivity = (from, to, specialtyId) => {
  const params = { from, to }
  if (specialtyId) params.specialtyId = specialtyId
  return client.get('/reports/doctor-productivity', { params })
}

export const getOfficeOccupancy = (from, to, specialtyId) => {
  const params = { from, to }
  if (specialtyId) params.specialtyId = specialtyId
  return client.get('/reports/office-occupancy', { params })
}

export const getNoShowPatients = (from, to, limit = 10) =>
  client.get('/reports/no-show-patients', { params: { from, to, limit } })

export const getSpecialties = () =>
  client.get('/specialties')
