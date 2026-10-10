import { calculateDistanceMeters } from './geo.js'

export function getClockFailureReason(position, location) {
  const accuracy = Number(position?.accuracy)
  const accuracyLimit = Number(location?.accuracyLimit)
  const allowedRadius = Number(location?.allowedRadius)

  if (!Number.isFinite(accuracy) || accuracy <= 0) {
    return '无法获取有效定位精度，请刷新定位后重试'
  }
  if (Number.isFinite(accuracyLimit) && accuracy > accuracyLimit) {
    return `当前定位精度为 ${accuracy.toFixed(2)} 米，超过精度上限 ${accuracyLimit} 米`
  }

  const distance = calculateDistanceMeters(position, location)
  if (distance === null) {
    return '无法计算当前位置与公司签到点的距离，请刷新定位后重试'
  }
  if (Number.isFinite(allowedRadius) && distance > allowedRadius) {
    return `当前位置距离公司签到点 ${distance.toFixed(2)} 米，超出公司允许范围，允许 ${allowedRadius} 米`
  }

  return ''
}
