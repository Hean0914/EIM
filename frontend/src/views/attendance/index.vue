<template>
  <section class="attendance-page">
    <div class="action-panel">
      <div>
        <h1>考勤打卡</h1>
        <p>上班打卡时间：{{ formatRuleTime(attendanceRule.workStartTime) }} 前；下班打卡时间：{{ formatRuleTime(attendanceRule.workEndTime) }} 后。</p>
        <p class="location-tip">
          打卡地点：{{ attendanceLocation.locationName || '暂未配置' }}
          <template v-if="attendanceLocation.address">（{{ attendanceLocation.address }}）</template>
          ，允许范围 {{ attendanceLocation.allowedRadius || '--' }} 米，定位精度不高于 {{ attendanceLocation.accuracyLimit || '--' }} 米。
        </p>
      </div>
      <div class="clock-actions">
        <el-button type="primary" :icon="Select" :loading="clockLoading" @click="handleCheckIn">上班打卡</el-button>
        <el-button type="success" :icon="CircleCheck" :loading="clockLoading" @click="handleCheckOut">下班打卡</el-button>
        <el-button v-if="isGeneralManager" :icon="Setting" @click="openRuleDialog">设置考勤时间</el-button>
        <el-button v-if="isGeneralManager" :icon="Location" @click="openLocationDialog">设置打卡地点</el-button>
      </div>
    </div>
    <div v-if="clockError" class="clock-state clock-state--error">{{ clockError }}</div>

    <div class="map-panel">
      <div class="map-panel__header">
        <div>
          <h2>签到位置</h2>
          <p>地图展示企业签到范围与当前设备位置，最终打卡结果以服务端校验为准。</p>
        </div>
        <el-button type="primary" :icon="Location" :loading="locationLoading" @click="refreshCurrentLocation">
          刷新定位
        </el-button>
      </div>
      <div v-if="mapError" class="map-state map-state--error">{{ mapError }}</div>
      <div ref="mapContainerRef" class="attendance-map" aria-label="企业考勤签到地图"></div>
      <div class="map-legend" aria-label="地图位置图例">
        <span><i class="legend-dot legend-dot--company"></i>公司签到位置</span>
        <span><i class="legend-dot legend-dot--employee"></i>当前员工位置</span>
      </div>
      <div class="location-points">
        <div class="location-point location-point--company">
          <div class="location-point__title">公司签到位置</div>
          <div>经度：{{ formatCoordinate(attendanceLocation.longitude) }}（GCJ-02）</div>
          <div>纬度：{{ formatCoordinate(attendanceLocation.latitude) }}（GCJ-02）</div>
          <div>地址：{{ attendanceLocation.address || '--' }}</div>
          <div>允许范围：{{ attendanceLocation.allowedRadius || '--' }} 米</div>
        </div>
        <div class="location-point location-point--employee">
          <div class="location-point__title">当前员工位置</div>
          <template v-if="currentPosition.longitude !== null">
          <div>经度：{{ formatCoordinate(currentPosition.longitude) }}（GCJ-02）</div>
          <div>纬度：{{ formatCoordinate(currentPosition.latitude) }}（GCJ-02）</div>
            <div>地址：{{ currentPosition.address || '--' }}</div>
            <div>定位精度：{{ formatMeters(currentPosition.accuracy) }}</div>
            <div>定位来源：{{ formatLocationType(currentPosition.locationType) }}</div>
          </template>
          <div v-else class="location-point__empty">请点击“刷新定位”获取当前位置</div>
        </div>
      </div>
      <div class="location-summary">
        <span><strong>经度</strong>{{ formatCoordinate(currentPosition.longitude) }}</span>
        <span><strong>纬度</strong>{{ formatCoordinate(currentPosition.latitude) }}</span>
        <span class="location-summary__address"><strong>地址</strong>{{ currentPosition.address || '--' }}</span>
        <span><strong>精度</strong>{{ formatMeters(currentPosition.accuracy) }}</span>
        <span><strong>来源</strong>{{ formatLocationType(currentPosition.locationType) }}</span>
        <span><strong>距签到点</strong>{{ formatMeters(currentDistance) }}</span>
      </div>
    </div>

    <section class="insight-panel">
      <div class="insight-header">
        <div>
          <h2>考勤日历与异常分析</h2>
          <p>用日历查看每天的出勤状态，用分析视图快速定位异常。</p>
        </div>
        <div class="insight-controls">
          <el-button circle :icon="ArrowLeft" title="上一个月" aria-label="上一个月" @click="shiftAnalysisMonth(-1)" />
          <el-date-picker
            v-model="analysisMonth"
            type="month"
            value-format="YYYY-MM"
            placeholder="选择月份"
            class="analysis-month-picker"
            @change="fetchAttendanceInsight"
          />
          <el-button circle :icon="ArrowRight" title="下一个月" aria-label="下一个月" @click="shiftAnalysisMonth(1)" />
          <el-select
            v-if="canViewAllAttendance"
            v-model="analysisQuery.employeeId"
            clearable
            filterable
            placeholder="全员概览"
            class="analysis-employee-select"
            @change="fetchAttendanceInsight"
          >
            <el-option v-for="item in employeeOptions" :key="item.id" :label="item.employeeName + '（' + item.employeeNo + '）'" :value="item.id" />
          </el-select>
          <el-button :icon="Refresh" :loading="insightLoading" @click="fetchAttendanceInsight">刷新分析</el-button>
        </div>
      </div>

      <el-tabs v-model="insightView" class="insight-tabs">
        <el-tab-pane label="考勤日历" name="calendar">
          <div v-loading="insightLoading" class="calendar-view">
            <div class="calendar-weekdays" aria-hidden="true">
              <span v-for="weekday in weekdays" :key="weekday">{{ weekday }}</span>
            </div>
            <div class="calendar-grid" aria-label="月度考勤日历">
              <button
                v-for="cell in calendarGrid"
                :key="cell.date"
                type="button"
                class="calendar-cell"
                :class="{ 'calendar-cell--muted': !cell.inMonth, [`calendar-cell--${calendarGroupTone(cell.group)}`]: cell.group }"
                @click="openCalendarDay(cell)"
              >
                <span class="calendar-cell__date">{{ cell.day }}</span>
                <template v-if="cell.group">
                  <span v-if="cell.group.total === 1" class="calendar-cell__status">
                    {{ statusLabel(cell.group.items[0].status) }}
                  </span>
                  <span v-else class="calendar-cell__status">
                    {{ cell.group.abnormal ? `异常 ${cell.group.abnormal} 人` : `${cell.group.total} 人有记录` }}
                  </span>
                  <span v-if="cell.group.total === 1" class="calendar-cell__meta">
                    {{ calendarItemMeta(cell.group.items[0]) }}
                  </span>
                  <span v-else class="calendar-cell__meta">
                    {{ cell.group.leave ? `请假 ${cell.group.leave}` : '' }}{{ cell.group.out ? ` 外出 ${cell.group.out}` : '' }}
                  </span>
                </template>
                <span v-else class="calendar-cell__empty">无记录</span>
              </button>
            </div>
            <div class="calendar-legend" aria-label="考勤状态图例">
              <span><i class="status-dot status-dot--success"></i>正常</span>
              <span><i class="status-dot status-dot--warning"></i>迟到/早退</span>
              <span><i class="status-dot status-dot--danger"></i>缺卡/缺勤</span>
              <span><i class="status-dot status-dot--neutral"></i>请假/外出/休息</span>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="异常分析" name="analysis">
          <div v-loading="insightLoading" class="analysis-view">
            <div class="analysis-cards">
              <article v-for="card in analysisCards" :key="card.key" class="analysis-card" :class="`analysis-card--${card.tone}`">
                <span>{{ card.label }}</span>
                <strong>{{ card.value }}</strong>
              </article>
            </div>
            <div class="analysis-summary">
              <span>统计范围 <strong>{{ formatDateRange(analysisMonth) }}</strong></span>
              <span>应出勤 <strong>{{ analysisResult.shouldWorkDays || 0 }}</strong> 天</span>
              <span>实际出勤 <strong>{{ analysisResult.actualWorkDays || 0 }}</strong> 天</span>
              <span>员工数 <strong>{{ analysisResult.employeeCount || 0 }}</strong></span>
            </div>
            <div class="analysis-main-grid">
              <div class="chart-panel">
                <div class="chart-panel__header">
                  <div>
                    <h3>每日异常趋势</h3>
                    <p>按日期查看异常人数和缺勤人数变化。</p>
                  </div>
                  <span class="chart-total">{{ totalTrendAnomalies }} 次异常</span>
                </div>
                <div class="trend-chart" aria-label="每日异常趋势图">
                  <div v-for="point in analysisResult.dailyTrend || []" :key="point.date" class="trend-column">
                    <div class="trend-column__bar-wrap">
                      <span class="trend-column__bar trend-column__bar--anomaly" :style="{ height: trendHeight(point.anomalyCount) }"></span>
                      <span class="trend-column__bar trend-column__bar--absence" :style="{ height: trendHeight(point.absentCount) }"></span>
                    </div>
                    <span class="trend-column__label">{{ dayNumber(point.date) }}</span>
                  </div>
                  <div v-if="!(analysisResult.dailyTrend || []).length" class="chart-empty">暂无趋势数据</div>
                </div>
                <div class="chart-key">
                  <span><i class="chart-key__dot chart-key__dot--anomaly"></i>异常</span>
                  <span><i class="chart-key__dot chart-key__dot--absence"></i>缺勤</span>
                </div>
              </div>
              <div class="chart-panel">
                <div class="chart-panel__header">
                  <div>
                    <h3>异常类型分布</h3>
                    <p>点击日历中的日期可查看具体原因。</p>
                  </div>
                </div>
                <div class="distribution-list">
                  <div v-for="item in analysisResult.anomalyDistribution || []" :key="item.type" class="distribution-item">
                    <div class="distribution-item__label"><span>{{ item.label }}</span><strong>{{ item.count }}</strong></div>
                    <div class="distribution-item__track"><span :style="{ width: distributionWidth(item.count) }"></span></div>
                  </div>
                  <div v-if="!(analysisResult.anomalyDistribution || []).length" class="chart-empty">暂无异常数据</div>
                </div>
              </div>
            </div>
            <div v-if="canViewAllAttendance" class="ranking-panel">
              <div class="chart-panel__header">
                <div>
                  <h3>员工异常排行</h3>
                  <p>优先展示异常次数较多的员工，便于跟进处理。</p>
                </div>
              </div>
              <div class="ranking-list">
                <div v-for="(item, index) in analysisResult.employeeRanking || []" :key="item.employeeId" class="ranking-item">
                  <span class="ranking-item__index">{{ index + 1 }}</span>
                  <div class="ranking-item__person"><strong>{{ item.employeeName || '未命名员工' }}</strong><span>{{ item.employeeNo || '--' }}</span></div>
                  <span class="ranking-item__rate">出勤率 {{ item.attendanceRate }}%</span>
                  <strong class="ranking-item__count">{{ item.anomalyCount }} 次异常</strong>
                </div>
                <div v-if="!(analysisResult.employeeRanking || []).length" class="chart-empty">暂无员工分析数据</div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </section>

    <el-drawer v-model="dayDrawerVisible" :title="`${selectedCalendarDay?.date || ''} 考勤详情`" size="440px">
      <div v-if="selectedCalendarDay?.group" class="day-drawer">
        <p class="day-drawer__summary">{{ selectedCalendarDay.group.total }} 人记录，{{ selectedCalendarDay.group.abnormal }} 人存在异常。</p>
        <article v-for="item in selectedCalendarDay.group.items" :key="`${item.employeeId}-${item.date}`" class="day-event">
          <div class="day-event__header">
            <div><strong>{{ item.employeeName || '当前员工' }}</strong><span>{{ item.employeeNo || '--' }}</span></div>
            <span class="status-pill" :class="`status-pill--${statusTone(item.status)}`">{{ statusLabel(item.status) }}</span>
          </div>
          <div class="day-event__grid">
            <span><small>上班</small>{{ formatDateTime(item.checkInTime) }}</span>
            <span><small>下班</small>{{ formatDateTime(item.checkOutTime) }}</span>
            <span><small>工作时长</small>{{ formatWorkMinutes(item.workMinutes) }}</span>
            <span><small>GPS</small>{{ gpsSummary(item) }}</span>
          </div>
          <p v-if="item.anomalies?.length" class="day-event__warning">异常：{{ anomalyLabels(item.anomalies) }}</p>
          <p v-if="item.approvedLeave" class="day-event__note">已通过请假申请</p>
          <p v-if="item.approvedOut" class="day-event__note">已通过外出申请{{ item.outDestination ? `：${item.outDestination}` : '' }}</p>
          <p v-if="item.remark" class="day-event__remark">备注：{{ item.remark }}</p>
        </article>
      </div>
      <el-empty v-else description="暂无考勤详情" />
    </el-drawer>

    <el-dialog v-model="ruleDialogVisible" title="设置企业考勤时间" width="460px" destroy-on-close>
      <el-form label-width="112px">
        <el-form-item label="上班打卡时间" required>
          <el-time-picker v-model="ruleForm.workStartTime" value-format="HH:mm:ss" format="HH:mm" placeholder="选择上班时间" />
        </el-form-item>
        <el-form-item label="下班打卡时间" required>
          <el-time-picker v-model="ruleForm.workEndTime" value-format="HH:mm:ss" format="HH:mm" placeholder="选择下班时间" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model.trim="ruleForm.remark" maxlength="255" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="ruleSaving" @click="saveAttendanceRule">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="locationDialogVisible" title="设置企业打卡地点" width="560px" destroy-on-close>
      <el-form ref="locationFormRef" :model="locationForm" :rules="locationRules" label-width="112px">
        <el-form-item label="地点名称" prop="locationName">
          <el-input v-model.trim="locationForm.locationName" maxlength="100" placeholder="例如：总部办公室" />
        </el-form-item>
        <el-form-item label="办公地址">
          <el-input v-model.trim="locationForm.address" maxlength="255" />
        </el-form-item>
          <el-form-item label="中心经度" prop="longitude">
            <el-input-number v-model="locationForm.longitude" :precision="7" :step="0.0000001" :min="-180" :max="180" controls-position="right" class="number-input" />
          </el-form-item>
          <el-form-item label="中心纬度" prop="latitude">
            <el-input-number v-model="locationForm.latitude" :precision="7" :step="0.0000001" :min="-90" :max="90" controls-position="right" class="number-input" />
          </el-form-item>
          <el-form-item label="坐标说明">
            <span class="coordinate-tip">请填写高德地图 GCJ-02 坐标，建议在公司现场使用“使用当前定位”自动填入。</span>
          </el-form-item>
          <el-form-item label="快速设置">
            <el-button :icon="Location" :loading="locationLoading" @click="useCurrentPositionForLocation">使用当前定位</el-button>
            <span class="coordinate-tip">请在公司签到点现场操作。</span>
          </el-form-item>
        <el-form-item label="允许半径" prop="allowedRadius">
          <el-input-number v-model="locationForm.allowedRadius" :min="10" :max="50000" controls-position="right" />
          <span class="unit-text">米</span>
        </el-form-item>
        <el-form-item label="精度上限" prop="accuracyLimit">
          <el-input-number v-model="locationForm.accuracyLimit" :min="5" :max="5000" controls-position="right" />
          <span class="unit-text">米</span>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model.trim="locationForm.remark" maxlength="255" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="locationDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="locationSaving" @click="saveAttendanceLocation">保存</el-button>
      </template>
    </el-dialog>

    <div class="table-panel">
      <el-form :model="query" inline class="search-form">
        <el-form-item v-if="canViewAllAttendance" label="员工">
          <el-select v-model="query.employeeId" clearable filterable placeholder="全部员工" class="employee-select">
            <el-option v-for="item in employeeOptions" :key="item.id" :label="item.employeeName + '（' + item.employeeNo + '）'" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="query.startDate" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="query.endDate" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" border stripe highlight-current-row row-key="id">
        <el-table-column v-if="canViewAllAttendance" prop="employeeNo" label="工号" width="120" />
        <el-table-column v-if="canViewAllAttendance" prop="employeeName" label="员工姓名" min-width="120" />
        <el-table-column v-if="canViewAllAttendance" prop="deptName" label="部门" min-width="130" />
        <el-table-column prop="attendanceDate" label="考勤日期" width="120" />
        <el-table-column prop="checkInTime" label="上班时间" min-width="170" />
        <el-table-column prop="checkOutTime" label="下班时间" min-width="170" />
        <el-table-column label="上班状态" width="100">
          <template #default="{ row }">{{ checkInStatusMap[row.checkInStatus] || '-' }}</template>
        </el-table-column>
        <el-table-column label="下班状态" width="100">
          <template #default="{ row }">{{ checkOutStatusMap[row.checkOutStatus] || '-' }}</template>
        </el-table-column>
        <el-table-column label="上班 GPS" width="150">
          <template #default="{ row }">{{ gpsStatusText(row.checkInGpsStatus, row.checkInDistance) }}</template>
        </el-table-column>
        <el-table-column label="下班 GPS" width="150">
          <template #default="{ row }">{{ gpsStatusText(row.checkOutGpsStatus, row.checkOutDistance) }}</template>
        </el-table-column>
        <el-table-column prop="workMinutes" label="工作分钟" width="110" />
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="query.current"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="fetchData"
        />
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, shallowRef } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowRight, CircleCheck, Location, Refresh, Search, Select, Setting } from '@element-plus/icons-vue'
import {
  checkIn,
  checkOut,
  getAttendanceAnalysis,
  getAttendanceLocation,
  getAttendanceRule,
  listAttendanceRecords,
  updateAttendanceLocation,
  updateAttendanceRule
} from '@/api/attendance'
import { listEmployeePage } from '@/api/employee'
import { useUserStore } from '@/store/user'
import { calculateDistanceMeters } from '@/utils/geo'
import { createAttendanceMap, getCurrentPosition } from '@/utils/amap'
import { getClockFailureReason } from '@/utils/attendance-location'
import { buildAnomalyCards, buildAttendanceCalendarGrid, statusLabel } from '@/utils/attendance-view'

const checkInStatusMap = { 0: '未打卡', 1: '正常', 2: '迟到', 3: '补卡' }
const checkOutStatusMap = { 0: '未打卡', 1: '正常', 2: '早退', 3: '补卡' }
const gpsStatusMap = { 1: '定位有效', 2: '超出范围', 3: '精度不足' }
const userStore = useUserStore()
const canViewAllAttendance = computed(() => userStore.canViewAllAttendance)
const isGeneralManager = computed(() => userStore.isGeneralManager)

const attendanceRule = reactive({ workStartTime: '', workEndTime: '', remark: '' })
const attendanceLocation = reactive({
  locationName: '',
  address: '',
  longitude: null,
  latitude: null,
  allowedRadius: null,
  accuracyLimit: null,
  remark: ''
})
const ruleForm = reactive({ workStartTime: '', workEndTime: '', remark: '' })
const locationForm = reactive({
  locationName: '',
  address: '',
  longitude: null,
  latitude: null,
  allowedRadius: 300,
  accuracyLimit: 100,
  remark: ''
})
const locationRules = {
  locationName: [{ required: true, message: '请输入打卡地点名称', trigger: 'blur' }],
  longitude: [{ required: true, message: '请输入中心经度', trigger: 'change' }],
  latitude: [{ required: true, message: '请输入中心纬度', trigger: 'change' }],
  allowedRadius: [{ required: true, message: '请输入允许打卡半径', trigger: 'change' }],
  accuracyLimit: [{ required: true, message: '请输入定位精度上限', trigger: 'change' }]
}
const query = reactive({ current: 1, size: 10, employeeId: null, startDate: '', endDate: '' })
const analysisQuery = reactive({ employeeId: null })
const analysisMonth = ref(currentMonth())
const insightView = ref('calendar')
const weekdays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const insightLoading = ref(false)
const analysisResult = ref({ calendar: [], dailyTrend: [], anomalyDistribution: [], employeeRanking: [] })
const dayDrawerVisible = ref(false)
const selectedCalendarDay = ref(null)
const loading = ref(false)
const clockLoading = ref(false)
const clockError = ref('')
const locationLoading = ref(false)
const employeeOptions = ref([])
const tableData = ref([])
const total = ref(0)
const ruleDialogVisible = ref(false)
const ruleSaving = ref(false)
const locationDialogVisible = ref(false)
const locationSaving = ref(false)
const locationFormRef = ref()
const mapContainerRef = ref()
const mapInstance = shallowRef(null)
const mapError = ref('')
const currentPosition = reactive({ longitude: null, latitude: null, accuracy: null, address: '', locationType: '' })
const currentDistance = computed(() => calculateDistanceMeters(currentPosition, attendanceLocation))
const calendarGrid = computed(() => buildAttendanceCalendarGrid(analysisMonth.value, analysisResult.value.calendar || []))
const analysisCards = computed(() => buildAnomalyCards(analysisResult.value))
const totalTrendAnomalies = computed(() => (analysisResult.value.dailyTrend || []).reduce((sum, item) => sum + Number(item.anomalyCount || 0), 0))
const maxTrendValue = computed(() => Math.max(1, ...(analysisResult.value.dailyTrend || []).map((item) => Number(item.anomalyCount || 0))))
const maxDistributionValue = computed(() => Math.max(1, ...(analysisResult.value.anomalyDistribution || []).map((item) => Number(item.count || 0))))
let refreshTimer = null

onMounted(async () => {
  await Promise.all([loadAttendanceRule(), loadAttendanceLocation()])
  await nextTick()
  await initializeAttendanceMap()
  if (canViewAllAttendance.value) {
    await loadEmployeeOptions()
  }
  await Promise.all([fetchData(), fetchAttendanceInsight()])
  if (canViewAllAttendance.value) {
    refreshTimer = window.setInterval(fetchData, 30000)
  }
})

onUnmounted(() => {
  if (refreshTimer) {
    window.clearInterval(refreshTimer)
  }
  mapInstance.value?.destroy()
  mapInstance.value = null
})

async function loadAttendanceRule() {
  const result = await getAttendanceRule()
  Object.assign(attendanceRule, result.data || {})
}

async function loadAttendanceLocation() {
  const result = await getAttendanceLocation()
  Object.assign(attendanceLocation, result.data || {})
}

async function initializeAttendanceMap() {
  if (!mapContainerRef.value) {
    return
  }
  if (!Number.isFinite(Number(attendanceLocation.longitude)) || !Number.isFinite(Number(attendanceLocation.latitude))) {
    mapError.value = '企业签到地点坐标未配置，暂时无法加载地图'
    return
  }

  mapError.value = ''
  try {
    if (!mapInstance.value) {
      mapInstance.value = await createAttendanceMap(mapContainerRef.value, attendanceLocation)
      return
    }
    mapInstance.value.updateAttendanceLocation(attendanceLocation)
  } catch (error) {
    mapError.value = error?.message || '地图加载失败，请检查高德 Key、域名白名单和网络配置'
  }
}

async function refreshCurrentLocation() {
  locationLoading.value = true
  mapError.value = ''
  try {
    const position = await getCurrentPosition()
    Object.assign(currentPosition, position)
    if (!mapInstance.value) {
      await initializeAttendanceMap()
    }
    if (!mapInstance.value) {
      return null
    }
    mapInstance.value.updateCurrentPosition(position)
    return position
  } catch (error) {
    clearCurrentPosition()
    mapError.value = error?.message || '无法获取当前位置，请检查浏览器定位权限'
    ElMessage.error(mapError.value)
    return null
  } finally {
    locationLoading.value = false
  }
}

function clearCurrentPosition() {
  mapInstance.value?.clearCurrentPosition()
  Object.assign(currentPosition, {
    longitude: null,
    latitude: null,
    accuracy: null,
    address: '',
    locationType: ''
  })
}

function formatRuleTime(value) {
  return value ? String(value).slice(0, 5) : '--:--'
}

function formatCoordinate(value) {
  return value === null || typeof value === 'undefined' ? '--' : Number(value).toFixed(7)
}

function formatMeters(value) {
  return value === null || typeof value === 'undefined' ? '--' : `${Number(value).toFixed(2)} 米`
}

function formatLocationType(value) {
  return {
    browser: '浏览器原生定位',
    unknown: '未提供定位来源'
  }[value] || value || '--'
}

function currentMonth() {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
}

function monthRange(month) {
  const [year, monthNumber] = String(month).split('-').map(Number)
  const lastDay = new Date(year, monthNumber, 0).getDate()
  return {
    startDate: `${year}-${String(monthNumber).padStart(2, '0')}-01`,
    endDate: `${year}-${String(monthNumber).padStart(2, '0')}-${String(lastDay).padStart(2, '0')}`
  }
}

function formatDateRange(month) {
  const range = monthRange(month)
  return `${range.startDate} 至 ${range.endDate}`
}

function shiftAnalysisMonth(step) {
  const [year, month] = analysisMonth.value.split('-').map(Number)
  const next = new Date(year, month - 1 + step, 1)
  analysisMonth.value = `${next.getFullYear()}-${String(next.getMonth() + 1).padStart(2, '0')}`
  fetchAttendanceInsight()
}

async function fetchAttendanceInsight() {
  insightLoading.value = true
  try {
    const result = await getAttendanceAnalysis({ ...monthRange(analysisMonth.value), ...analysisQuery })
    analysisResult.value = result.data || { calendar: [], dailyTrend: [], anomalyDistribution: [], employeeRanking: [] }
  } finally {
    insightLoading.value = false
  }
}

function openCalendarDay(cell) {
  selectedCalendarDay.value = cell
  dayDrawerVisible.value = true
}

function calendarGroupTone(group) {
  if (!group) return 'empty'
  if (group.abnormal > 0) return 'danger'
  if (group.leave > 0 || group.out > 0) return 'neutral'
  return 'success'
}

function statusTone(status) {
  if (status === 'NORMAL') return 'success'
  if (status === 'LEAVE' || status === 'OUT' || status === 'REST') return 'neutral'
  if (status === 'ABNORMAL' || status === 'LATE' || status === 'EARLY_LEAVE') return 'warning'
  return 'danger'
}

function calendarItemMeta(item) {
  if (item.status === 'LEAVE') return '已通过请假'
  if (item.status === 'OUT') return '已通过外出'
  if (item.status === 'REST') return '休息日'
  if (item.checkInTime || item.checkOutTime) {
    return `${formatTime(item.checkInTime)} / ${formatTime(item.checkOutTime)}`
  }
  return '待补充'
}

function formatTime(value) {
  return value ? String(value).slice(11, 16) : '--:--'
}

function formatDateTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : '--'
}

function formatWorkMinutes(value) {
  if (value === null || typeof value === 'undefined') return '--'
  const minutes = Number(value)
  return `${Math.floor(minutes / 60)} 小时 ${minutes % 60} 分钟`
}

function gpsSummary(item) {
  const statuses = [item.checkInGpsStatus, item.checkOutGpsStatus].filter(Boolean)
  if (!statuses.length) return '--'
  return statuses.some((status) => status === 2 || status === 3) ? '异常' : '有效'
}

function anomalyLabels(items = []) {
  const labels = {
    LATE: '迟到',
    EARLY_LEAVE: '早退',
    MISSING_CHECK_IN: '上班缺卡',
    MISSING_CHECK_OUT: '下班缺卡',
    ABSENCE: '缺勤',
    GPS_ABNORMAL: 'GPS 异常'
  }
  return items.map((item) => labels[item] || item).join('、')
}

function dayNumber(value) {
  return value ? String(value).slice(8, 10) : '--'
}

function trendHeight(value) {
  return `${Math.max(4, Number(value || 0) / maxTrendValue.value * 100)}%`
}

function distributionWidth(value) {
  return `${Math.max(4, Number(value || 0) / maxDistributionValue.value * 100)}%`
}

function openRuleDialog() {
  Object.assign(ruleForm, {
    workStartTime: attendanceRule.workStartTime,
    workEndTime: attendanceRule.workEndTime,
    remark: attendanceRule.remark || ''
  })
  ruleDialogVisible.value = true
}

function openLocationDialog() {
  Object.assign(locationForm, {
    locationName: attendanceLocation.locationName || '',
    address: attendanceLocation.address || '',
    longitude: Number(attendanceLocation.longitude),
    latitude: Number(attendanceLocation.latitude),
    allowedRadius: attendanceLocation.allowedRadius || 300,
    accuracyLimit: attendanceLocation.accuracyLimit || 100,
    remark: attendanceLocation.remark || ''
  })
  locationDialogVisible.value = true
}

async function useCurrentPositionForLocation() {
  const position = await refreshCurrentLocation()
  if (!position) {
    return
  }
  locationForm.longitude = position.longitude
  locationForm.latitude = position.latitude
  ElMessage.success('已填入当前 GCJ-02 定位坐标，请确认后保存')
}

async function saveAttendanceRule() {
  if (!ruleForm.workStartTime || !ruleForm.workEndTime) {
    ElMessage.warning('请设置上班和下班打卡时间')
    return
  }
  ruleSaving.value = true
  try {
    await updateAttendanceRule({ ...ruleForm })
    await loadAttendanceRule()
    ElMessage.success('考勤时间已更新')
    ruleDialogVisible.value = false
  } finally {
    ruleSaving.value = false
  }
}

async function saveAttendanceLocation() {
  const valid = await locationFormRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  locationSaving.value = true
  try {
    await updateAttendanceLocation({ ...locationForm })
    await loadAttendanceLocation()
    await initializeAttendanceMap()
    ElMessage.success('打卡地点已更新')
    locationDialogVisible.value = false
  } finally {
    locationSaving.value = false
  }
}

async function handleCheckIn() {
  await clock('checkIn')
}

async function handleCheckOut() {
  await clock('checkOut')
}

async function clock(action) {
  clockLoading.value = true
  clockError.value = ''
  try {
    const position = currentPosition.longitude !== null
      ? { ...currentPosition }
      : await refreshCurrentLocation()
    if (!position) {
      return
    }
    const localFailureReason = getClockFailureReason(position, attendanceLocation)
    if (localFailureReason) {
      clockError.value = localFailureReason
      ElMessage.error(localFailureReason)
      return
    }
    const payload = {
      longitude: position.longitude,
      latitude: position.latitude,
      accuracy: position.accuracy,
      address: position.address
    }
    if (action === 'checkIn') {
      await checkIn(payload)
      ElMessage.success('上班打卡成功')
    } else {
      await checkOut(payload)
      ElMessage.success('下班打卡成功')
    }
    fetchData()
  } catch (error) {
    clockError.value = error?.message || '定位失败，请检查高德 Key、浏览器定位权限、系统定位服务和网络连接'
    ElMessage.error(clockError.value)
  } finally {
    clockLoading.value = false
  }
}

function gpsStatusText(status, distance) {
  if (!status) {
    return '-'
  }
  const text = gpsStatusMap[status] || '未知'
  return distance === null || typeof distance === 'undefined'
    ? text
    : text + '（' + Number(distance).toFixed(2) + ' 米）'
}

async function loadEmployeeOptions() {
  const result = await listEmployeePage({ current: 1, size: 100, employmentStatus: 1 })
  employeeOptions.value = result.data?.records || []
}

async function fetchData() {
  loading.value = true
  try {
    const result = await listAttendanceRecords(cleanQuery())
    const data = result.data || {}
    tableData.value = data.records || []
    total.value = Number(data.total || 0)
  } finally {
    loading.value = false
  }
}

function cleanQuery() {
  return Object.fromEntries(
    Object.entries(query).filter(([, value]) => value !== '' && value !== null && typeof value !== 'undefined')
  )
}

function handleSearch() {
  query.current = 1
  fetchData()
}

function handleReset() {
  query.current = 1
  query.size = 10
  query.employeeId = null
  query.startDate = ''
  query.endDate = ''
  fetchData()
}

function handleSizeChange() {
  query.current = 1
  fetchData()
}
</script>

<style scoped>
.attendance-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.action-panel,
.map-panel,
.insight-panel,
.table-panel {
  padding: 20px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.insight-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.insight-header,
.chart-panel__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.insight-header h2,
.chart-panel__header h3 {
  margin: 0;
  color: #111827;
  font-weight: 600;
}

.insight-header h2 {
  font-size: 18px;
}

.chart-panel__header h3 {
  font-size: 16px;
}

.insight-header p,
.chart-panel__header p {
  margin: 6px 0 0;
  color: #6b7280;
  font-size: 13px;
  line-height: 1.6;
}

.insight-controls {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 8px;
}

.analysis-month-picker {
  width: 132px;
}

.analysis-employee-select {
  width: 220px;
}

.insight-tabs :deep(.el-tabs__header) {
  margin-bottom: 18px;
}

.calendar-view,
.analysis-view {
  min-height: 320px;
}

.calendar-weekdays,
.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
}

.calendar-weekdays {
  gap: 8px;
  margin-bottom: 8px;
  color: #6b7280;
  font-size: 12px;
  text-align: center;
}

.calendar-grid {
  gap: 8px;
}

.calendar-cell {
  min-height: 94px;
  padding: 10px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #fff;
  color: #374151;
  text-align: left;
  cursor: pointer;
  transition: border-color 160ms ease, box-shadow 160ms ease, transform 160ms ease;
}

.calendar-cell:hover,
.calendar-cell:focus-visible {
  border-color: #93c5fd;
  box-shadow: 0 3px 10px rgba(37, 99, 235, 0.12);
  outline: none;
  transform: translateY(-1px);
}

.calendar-cell--muted {
  color: #9ca3af;
  background: #f9fafb;
}

.calendar-cell__date {
  display: block;
  margin-bottom: 11px;
  color: #111827;
  font-size: 14px;
  font-weight: 600;
}

.calendar-cell--muted .calendar-cell__date {
  color: #9ca3af;
}

.calendar-cell__status,
.calendar-cell__meta,
.calendar-cell__empty {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.calendar-cell__status {
  color: #111827;
  font-size: 13px;
  font-weight: 600;
}

.calendar-cell__meta,
.calendar-cell__empty {
  margin-top: 5px;
  color: #6b7280;
  font-size: 12px;
}

.calendar-cell--success {
  border-top: 3px solid #16a34a;
}

.calendar-cell--warning {
  border-top: 3px solid #d97706;
}

.calendar-cell--danger {
  border-top: 3px solid #dc2626;
}

.calendar-cell--neutral {
  border-top: 3px solid #64748b;
}

.calendar-legend,
.chart-key {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 14px;
  color: #6b7280;
  font-size: 12px;
}

.calendar-legend span,
.chart-key span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.status-dot,
.chart-key__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-dot--success,
.chart-key__dot--anomaly {
  background: #16a34a;
}

.status-dot--warning {
  background: #d97706;
}

.status-dot--danger,
.chart-key__dot--absence {
  background: #dc2626;
}

.status-dot--neutral {
  background: #64748b;
}

.analysis-cards {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 10px;
}

.analysis-card {
  min-height: 84px;
  padding: 14px;
  border-left: 3px solid #2563eb;
  border-radius: 6px;
  background: #f8fafc;
}

.analysis-card span,
.analysis-card strong {
  display: block;
}

.analysis-card span {
  color: #6b7280;
  font-size: 12px;
}

.analysis-card strong {
  margin-top: 10px;
  color: #111827;
  font-size: 22px;
  line-height: 1;
}

.analysis-card--primary { border-left-color: #2563eb; background: #eff6ff; }
.analysis-card--warning { border-left-color: #d97706; background: #fffbeb; }
.analysis-card--danger { border-left-color: #dc2626; background: #fef2f2; }
.analysis-card--info { border-left-color: #0891b2; background: #ecfeff; }

.analysis-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 24px;
  padding: 12px 14px;
  border-top: 1px solid #e5e7eb;
  border-bottom: 1px solid #e5e7eb;
  color: #6b7280;
  font-size: 13px;
}

.analysis-summary strong {
  margin-left: 4px;
  color: #111827;
  font-weight: 600;
}

.analysis-main-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(280px, 0.7fr);
  gap: 14px;
}

.chart-panel,
.ranking-panel {
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
}

.chart-total {
  color: #dc2626;
  font-size: 13px;
  font-weight: 600;
}

.trend-chart {
  display: flex;
  align-items: flex-end;
  gap: 6px;
  height: 176px;
  margin-top: 18px;
  padding: 10px 4px 0;
  border-bottom: 1px solid #d1d5db;
  overflow-x: auto;
}

.trend-column {
  display: flex;
  align-items: center;
  flex: 1 0 18px;
  flex-direction: column;
  justify-content: flex-end;
  min-width: 18px;
  height: 100%;
}

.trend-column__bar-wrap {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 2px;
  width: 100%;
  height: calc(100% - 18px);
}

.trend-column__bar {
  display: block;
  width: 7px;
  min-height: 4px;
  border-radius: 3px 3px 0 0;
}

.trend-column__bar--anomaly { background: #f59e0b; }
.trend-column__bar--absence { background: #dc2626; }

.trend-column__label {
  margin-top: 6px;
  color: #9ca3af;
  font-size: 10px;
}

.distribution-list,
.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 13px;
  margin-top: 20px;
}

.distribution-item__label {
  display: flex;
  justify-content: space-between;
  color: #4b5563;
  font-size: 13px;
}

.distribution-item__label strong {
  color: #111827;
}

.distribution-item__track {
  height: 7px;
  margin-top: 6px;
  overflow: hidden;
  border-radius: 4px;
  background: #f3f4f6;
}

.distribution-item__track span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: #2563eb;
}

.ranking-panel {
  margin-top: 14px;
}

.ranking-item {
  display: grid;
  grid-template-columns: 28px minmax(0, 1fr) auto auto;
  align-items: center;
  gap: 10px;
  padding-bottom: 11px;
  border-bottom: 1px solid #f3f4f6;
}

.ranking-item:last-child {
  padding-bottom: 0;
  border-bottom: 0;
}

.ranking-item__index {
  color: #9ca3af;
  font-size: 12px;
  text-align: center;
}

.ranking-item__person strong,
.ranking-item__person span {
  display: block;
}

.ranking-item__person strong {
  color: #111827;
  font-size: 13px;
}

.ranking-item__person span,
.ranking-item__rate {
  color: #6b7280;
  font-size: 12px;
}

.ranking-item__count {
  color: #dc2626;
  font-size: 13px;
}

.chart-empty {
  width: 100%;
  padding: 36px 0;
  color: #9ca3af;
  font-size: 13px;
  text-align: center;
}

.day-drawer__summary {
  margin: 0 0 16px;
  color: #6b7280;
  font-size: 13px;
}

.day-event {
  padding: 14px 0;
  border-bottom: 1px solid #e5e7eb;
}

.day-event:first-of-type {
  padding-top: 0;
}

.day-event__header,
.day-event__header > div {
  display: flex;
  align-items: center;
}

.day-event__header {
  justify-content: space-between;
  gap: 12px;
}

.day-event__header > div {
  min-width: 0;
  gap: 8px;
}

.day-event__header strong {
  color: #111827;
  font-size: 14px;
}

.day-event__header span:not(.status-pill) {
  color: #9ca3af;
  font-size: 12px;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  flex: 0 0 auto;
  min-height: 24px;
  padding: 0 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.status-pill--success { color: #166534; background: #dcfce7; }
.status-pill--warning { color: #92400e; background: #fef3c7; }
.status-pill--danger { color: #991b1b; background: #fee2e2; }
.status-pill--neutral { color: #475569; background: #f1f5f9; }

.day-event__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-top: 14px;
  color: #374151;
  font-size: 13px;
}

.day-event__grid span,
.day-event__grid small {
  display: block;
}

.day-event__grid small {
  margin-bottom: 4px;
  color: #9ca3af;
  font-size: 11px;
}

.day-event__warning,
.day-event__note,
.day-event__remark {
  margin: 12px 0 0;
  font-size: 12px;
  line-height: 1.6;
}

.day-event__warning { color: #b45309; }
.day-event__note { color: #2563eb; }
.day-event__remark { color: #6b7280; }

.clock-state {
  padding: 10px 12px;
  border-radius: 6px;
  font-size: 13px;
  line-height: 1.6;
}

.clock-state--error {
  color: #b42318;
  background: #fef3f2;
  border: 1px solid #fecdca;
}

.map-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.map-panel__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.map-panel__header h2 {
  margin: 0;
  color: #111827;
  font-size: 18px;
  font-weight: 600;
}

.map-panel__header p {
  margin: 6px 0 0;
  color: #6b7280;
  font-size: 13px;
  line-height: 1.6;
}

.attendance-map {
  width: 100%;
  height: 360px;
  overflow: hidden;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background: #f3f4f6;
}

.map-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  color: #4b5563;
  font-size: 13px;
}

.map-legend span {
  display: inline-flex;
  align-items: center;
  gap: 7px;
}

.legend-dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
}

.legend-dot--company {
  background: #2563eb;
}

.legend-dot--employee {
  background: #dc2626;
}

.location-points {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.location-point {
  padding: 12px 14px;
  border: 1px solid #e5e7eb;
  border-left-width: 3px;
  border-radius: 6px;
  color: #374151;
  font-size: 13px;
  line-height: 1.7;
}

.location-point--company {
  border-left-color: #2563eb;
}

.location-point--employee {
  border-left-color: #dc2626;
}

.location-point__title {
  margin-bottom: 4px;
  color: #111827;
  font-size: 14px;
  font-weight: 600;
}

.location-point__empty {
  color: #6b7280;
}

.map-state {
  padding: 10px 12px;
  border-radius: 6px;
  font-size: 13px;
  line-height: 1.6;
}

.map-state--error {
  color: #b42318;
  background: #fef3f2;
  border: 1px solid #fecdca;
}

.location-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px 20px;
  color: #374151;
  font-size: 13px;
  line-height: 1.6;
}

.location-summary span {
  min-width: 0;
  overflow-wrap: anywhere;
}

.location-summary strong {
  margin-right: 6px;
  color: #6b7280;
  font-weight: 500;
}

.location-summary__address {
  grid-column: span 2;
}

.action-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.action-panel h1 {
  margin: 0;
  color: #111827;
  font-size: 20px;
  font-weight: 600;
}

.action-panel p {
  margin: 6px 0 0;
  color: #4b5563;
  font-size: 14px;
  line-height: 1.6;
}

.action-panel .location-tip {
  color: #374151;
}

.clock-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.number-input {
  width: 100%;
}

.unit-text {
  margin-left: 8px;
  color: #6b7280;
  font-size: 13px;
}

.coordinate-tip {
  color: #6b7280;
  font-size: 13px;
  line-height: 1.6;
}

.search-form {
  margin-bottom: 16px;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.employee-select {
  width: 220px;
}

@media (max-width: 900px) {
  .action-panel {
    align-items: flex-start;
    flex-direction: column;
  }

  .clock-actions {
    justify-content: flex-start;
  }

  .map-panel__header {
    align-items: flex-start;
    flex-direction: column;
  }

  .map-panel__header .el-button {
    width: 100%;
  }

  .location-summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .location-summary__address {
    grid-column: span 2;
  }

  .location-points {
    grid-template-columns: 1fr;
  }

  .insight-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .insight-controls {
    justify-content: flex-start;
    width: 100%;
  }

  .analysis-cards {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .analysis-main-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 600px) {
  .calendar-grid,
  .calendar-weekdays {
    gap: 4px;
  }

  .calendar-cell {
    min-height: 78px;
    padding: 7px;
  }

  .calendar-cell__date {
    margin-bottom: 7px;
  }

  .calendar-cell__status,
  .calendar-cell__meta,
  .calendar-cell__empty {
    font-size: 10px;
  }

  .analysis-cards {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .analysis-summary {
    gap: 8px 14px;
  }

  .ranking-item {
    grid-template-columns: 24px minmax(0, 1fr) auto;
  }

  .ranking-item__rate {
    grid-column: 2;
  }

  .ranking-item__count {
    grid-column: 3;
    grid-row: 1 / span 2;
  }

  .analysis-month-picker,
  .analysis-employee-select {
    width: 100%;
  }

  .insight-controls > .el-button:not(.is-circle) {
    flex: 1 1 100%;
  }
}
</style>
