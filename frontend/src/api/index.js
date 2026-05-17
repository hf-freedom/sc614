import axios from 'axios'

const request = axios.create({
  baseURL: '/api/parking',
  timeout: 5000
})

export default {
  getOverview() {
    return request.get('/overview')
  },
  vehicleEntry(data) {
    return request.post('/entry', data)
  },
  calculateFee(licensePlate) {
    return request.get(`/fee/${licensePlate}`)
  },
  payFee(data) {
    return request.post('/pay', data)
  },
  vehicleExit(data) {
    return request.post('/exit', data)
  },
  getSpots() {
    return request.get('/spots')
  },
  adjustSpot(data) {
    return request.put('/spots/adjust', data)
  },
  getRecords() {
    return request.get('/records')
  },
  getMonthlyVehicles() {
    return request.get('/monthly')
  },
  addMonthlyVehicle(data) {
    return request.post('/monthly', data)
  },
  getExpiringVehicles() {
    return request.get('/monthly/expiring')
  },
  getBlacklist() {
    return request.get('/blacklist')
  },
  addToBlacklist(data) {
    return request.post('/blacklist', data)
  },
  removeFromBlacklist(licensePlate) {
    return request.delete(`/blacklist/${licensePlate}`)
  },
  getAlarms() {
    return request.get('/alarms')
  },
  getStatistics() {
    return request.get('/statistics')
  }
}
