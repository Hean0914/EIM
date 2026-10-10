import test from 'node:test'
import assert from 'node:assert/strict'
import { calculateDistanceMeters } from '../src/utils/geo.js'
import { getAmapGeolocationErrorMessage, normalizeAmapPosition } from '../src/utils/amap.js'
import { getClockFailureReason } from '../src/utils/attendance-location.js'

test('calculates distance between two GCJ-02 points in meters', () => {
  const distance = calculateDistanceMeters(
    { longitude: 113.851551, latitude: 22.883835 },
    { longitude: 113.852551, latitude: 22.883835 }
  )

  assert.ok(distance > 100)
  assert.ok(distance < 120)
})

test('returns null for incomplete coordinates', () => {
  assert.equal(
    calculateDistanceMeters(
      { longitude: 113.851551, latitude: 22.883835 },
      { longitude: null, latitude: 22.883835 }
    ),
    null
  )
})

test('normalizes AMap HTML5 geolocation coordinates', () => {
  assert.deepEqual(
    normalizeAmapPosition({
      position: { lng: 113.851551, lat: 22.883835 },
      accuracy: 24.5,
      formattedAddress: '广东省东莞市松山湖',
      location_type: 'html5'
    }),
    {
      longitude: 113.851551,
      latitude: 22.883835,
      accuracy: 24.5,
      address: '广东省东莞市松山湖',
      coordinateSystem: 'GCJ-02',
      locationType: 'html5'
    }
  )
})

test('rejects AMap geolocation without usable coordinates', () => {
  assert.throws(
    () => normalizeAmapPosition({ position: { lng: null, lat: 22.883835 }, accuracy: 20 }),
    /高德定位返回的坐标或精度无效/
  )
})

test('rejects AMap IP location for attendance clocking', () => {
  assert.throws(
    () => normalizeAmapPosition({
      position: { lng: 113.851887, lat: 22.884577 },
      accuracy: 500,
      location_type: 'ip'
    }),
    /高德返回的是网络IP位置/
  )
})

test('explains AMap geolocation permission denial in Chinese', () => {
  assert.equal(
    getAmapGeolocationErrorMessage({ message: 'Geolocation permission denied.' }),
    '定位权限被拒绝，请在浏览器地址栏中允许位置权限后重试'
  )
})

test('explains when the employee is outside the company attendance radius', () => {
  const reason = getClockFailureReason(
    {
      longitude: 110.376674,
      latitude: 25.277524,
      accuracy: 481
    },
    {
      longitude: 113.851551,
      latitude: 22.883835,
      allowedRadius: 3000,
      accuracyLimit: 3000
    }
  )

  assert.match(reason, /超出公司允许范围/)
  assert.match(reason, /允许 3000 米/)
})
