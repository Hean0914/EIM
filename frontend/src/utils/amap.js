let amapLoaderPromise

export function loadAmap() {
  if (window.AMap) {
    return Promise.resolve(window.AMap)
  }
  if (amapLoaderPromise) {
    return amapLoaderPromise
  }

  const key = import.meta.env.VITE_AMAP_KEY
  const securityJsCode = import.meta.env.VITE_AMAP_SECURITY_CODE
  if (!key) {
    return Promise.reject(new Error('未配置高德地图 Key，请在 frontend/.env.local 中设置 VITE_AMAP_KEY'))
  }

  if (securityJsCode) {
    window._AMapSecurityConfig = {
      ...window._AMapSecurityConfig,
      securityJsCode
    }
  }

  amapLoaderPromise = new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${encodeURIComponent(key)}&plugin=AMap.Geolocation,AMap.Scale,AMap.ToolBar`
    script.async = true
    script.onload = () => window.AMap ? resolve(window.AMap) : reject(new Error('高德地图脚本加载失败'))
    script.onerror = () => reject(new Error('高德地图脚本加载失败，请检查 Key、域名白名单和网络配置'))
    document.head.appendChild(script)
  }).catch(error => {
    amapLoaderPromise = undefined
    throw error
  })

  return amapLoaderPromise
}

export function normalizeAmapPosition(result) {
  const locationType = String(result?.location_type || result?.locationType || '').toLowerCase()
  if (['ip', 'ipcity', 'ip_city'].includes(locationType)) {
    throw new Error('高德返回的是网络IP位置，无法用于考勤打卡，请开启设备定位后重试')
  }

  const rawLongitude = result?.position?.getLng?.() ?? result?.position?.lng
  const rawLatitude = result?.position?.getLat?.() ?? result?.position?.lat
  const rawAccuracy = result?.accuracy
  if ([rawLongitude, rawLatitude, rawAccuracy].some(value => value === null || typeof value === 'undefined' || value === '')) {
    throw new Error('高德定位返回的坐标或精度无效，请刷新定位后重试')
  }

  const longitude = Number(rawLongitude)
  const latitude = Number(rawLatitude)
  const accuracy = Number(rawAccuracy)
  if (!Number.isFinite(longitude) || !Number.isFinite(latitude) || !Number.isFinite(accuracy)
    || accuracy <= 0 || longitude < -180 || longitude > 180 || latitude < -90 || latitude > 90) {
    throw new Error('高德定位返回的坐标或精度无效，请刷新定位后重试')
  }

  return {
    longitude,
    latitude,
    accuracy,
    address: result.formattedAddress || result.addressComponent?.building || '',
    coordinateSystem: 'GCJ-02',
    locationType: locationType || 'unknown'
  }
}

export async function getCurrentPosition() {
  const AMap = await loadAmap()
  return new Promise((resolve, reject) => {
    const geolocation = new AMap.Geolocation({
      enableHighAccuracy: true,
      timeout: 15000,
      maximumAge: 0,
      convert: true,
      needAddress: true,
      extensions: 'all',
      GeoLocationFirst: true,
      noIpLocate: 3,
      noGeoLocation: 0,
      showButton: false,
      showMarker: false,
      showCircle: false
    })

    geolocation.getCurrentPosition((status, result) => {
      if (status === 'complete' && result?.position) {
        try {
          resolve(normalizeAmapPosition(result))
        } catch (error) {
          reject(error)
        }
        return
      }
      reject(new Error(getAmapGeolocationErrorMessage(result)))
    })
  })
}

export function getAmapGeolocationErrorMessage(result) {
  const message = String(result?.message || result?.info || '')
  if (/permission denied|permissiondenied/i.test(message)) {
    return '定位权限被拒绝，请在浏览器地址栏中允许位置权限后重试'
  }
  if (/timeout/i.test(message)) {
    return '获取定位超时，请确认系统定位服务已开启后重试'
  }
  if (/position unavailable|unavailable/i.test(message)) {
    return '当前无法获取设备位置，请开启系统定位服务并确认网络可用'
  }
  return message || '高德无法获取当前位置，请检查定位权限和系统定位服务'
}

export async function createAttendanceMap(container, location) {
  const AMap = await loadAmap()
  const companyPosition = toPosition(location)
  if (!container || !companyPosition) {
    throw new Error('企业签到地点坐标未配置，暂时无法加载地图')
  }

  const map = new AMap.Map(container, {
    zoom: 16,
    center: companyPosition,
    viewMode: '2D'
  })
  map.addControl(new AMap.Scale())
  map.addControl(new AMap.ToolBar({ position: 'RB' }))

  let companyMarker
  let companyRange
  let employeeMarker
  let employeeAccuracyRange

  function clearOverlay(overlay) {
    overlay?.setMap(null)
  }

  function fitView() {
    map.setFitView(
      [companyMarker, companyRange, employeeMarker, employeeAccuracyRange].filter(Boolean),
      false,
      [40, 40, 40, 40]
    )
  }

  const controller = {
    map,
    updateAttendanceLocation(nextLocation) {
      const position = toPosition(nextLocation)
      if (!position) {
        throw new Error('企业签到地点坐标未配置，暂时无法加载地图')
      }
      clearOverlay(companyMarker)
      clearOverlay(companyRange)
      companyMarker = new AMap.Marker({
        position,
        title: nextLocation.locationName || '公司签到位置',
        content: createMarkerContent('公司签到位置', '#2563eb'),
        offset: new AMap.Pixel(-54, -45),
        zIndex: 100
      })
      companyRange = new AMap.Circle({
        center: position,
        radius: Number(nextLocation.allowedRadius || 0),
        strokeColor: '#2563eb',
        strokeOpacity: 0.85,
        strokeWeight: 2,
        fillColor: '#2563eb',
        fillOpacity: 0.12
      })
      map.add([companyMarker, companyRange])
      fitView()
    },
    updateCurrentPosition(position) {
      const point = toPosition(position)
      if (!point) {
        throw new Error('当前定位坐标无效，请刷新定位后重试')
      }
      clearOverlay(employeeMarker)
      clearOverlay(employeeAccuracyRange)
      employeeMarker = new AMap.Marker({
        position: point,
        title: '当前员工位置',
        content: createMarkerContent('当前员工位置', '#dc2626'),
        offset: new AMap.Pixel(-54, -45),
        zIndex: 110
      })
      employeeAccuracyRange = new AMap.Circle({
        center: point,
        radius: Number(position.accuracy),
        strokeColor: '#dc2626',
        strokeOpacity: 0.75,
        strokeStyle: 'dashed',
        strokeWeight: 1,
        fillColor: '#dc2626',
        fillOpacity: 0.08
      })
      map.add([employeeMarker, employeeAccuracyRange])
      fitView()
    },
    clearCurrentPosition() {
      clearOverlay(employeeMarker)
      clearOverlay(employeeAccuracyRange)
      employeeMarker = undefined
      employeeAccuracyRange = undefined
    },
    destroy() {
      map.destroy()
    }
  }

  controller.updateAttendanceLocation(location)
  return controller
}

function toPosition(location) {
  const longitude = Number(location?.longitude)
  const latitude = Number(location?.latitude)
  return Number.isFinite(longitude) && Number.isFinite(latitude) ? [longitude, latitude] : null
}

function createMarkerContent(label, color) {
  return `<div style="display:flex;align-items:center;gap:6px;padding:6px 9px;border:1px solid ${color};border-radius:6px;background:#fff;color:#111827;font-size:12px;font-weight:600;white-space:nowrap;box-shadow:0 1px 3px rgba(15,23,42,.18)"><span style="width:9px;height:9px;border-radius:50%;background:${color};display:inline-block"></span>${label}</div>`
}
