export function calculateDistanceMeters(from, to) {
  const rawValues = [from?.longitude, from?.latitude, to?.longitude, to?.latitude]
  if (rawValues.some(value => value === null || typeof value === 'undefined' || value === '')) {
    return null
  }
  const values = rawValues.map(Number)
  if (values.some(value => !Number.isFinite(value))) {
    return null
  }

  const [fromLongitude, fromLatitude, toLongitude, toLatitude] = values
  const earthRadius = 6371000
  const latitudeDelta = (toLatitude - fromLatitude) * Math.PI / 180
  const longitudeDelta = (toLongitude - fromLongitude) * Math.PI / 180
  const a = Math.sin(latitudeDelta / 2) ** 2
    + Math.cos(fromLatitude * Math.PI / 180)
      * Math.cos(toLatitude * Math.PI / 180)
      * Math.sin(longitudeDelta / 2) ** 2

  return earthRadius * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
}
