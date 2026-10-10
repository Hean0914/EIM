import { computed, onScopeDispose, ref, watch } from 'vue'

const initialTasks = [
  { id: 'files', status: 'pending', category: '行政事务', task: '整理员工档案', progress: 0, deadline: '本周五截止', owner: '李' },
  { id: 'summary', status: 'active', category: '团队协作', task: '季度工作总结', progress: 65, deadline: '本周四截止', owner: '陈' },
  { id: 'inspection', status: 'done', category: '日常办公', task: '设备巡检记录', progress: 100, deadline: '已按期完成', owner: '王' }
]

export const demoDays = [
  { key: 'monday', label: '10月12日', weekday: '周一' },
  { key: 'tuesday', label: '10月13日', weekday: '周二' },
  { key: 'wednesday', label: '10月14日', weekday: '周三' }
]

const exampleMeetings = [
  { id: 'weekly', day: 'monday', start: '09:30', end: '10:00', title: '部门工作例会', location: '一号会议室', attendees: '部门负责人、各组组长', agenda: '同步本周工作安排，确认各组待办。' },
  { id: 'project', day: 'monday', start: '14:00', end: '15:00', title: '项目进度讨论', location: '二号会议室', attendees: '项目经理、研发组成员', agenda: '检查里程碑进度，协调当前阻塞事项。' },
  { id: 'review', day: 'tuesday', start: '10:00', end: '11:00', title: '方案评审', location: '二号会议室', attendees: '项目经理、产品与研发负责人', agenda: '评审交付方案，记录待完善事项。' },
  { id: 'training', day: 'tuesday', start: '15:00', end: '16:00', title: '新员工培训', location: '培训室', attendees: '人事专员、新入职员工', agenda: '介绍考勤规范与内部审批流程。' }
]

export function useLoginDemo() {
  const activePreview = ref('plan')
  const tasks = ref(initialTasks.map((task) => ({ ...task })))
  const approvalStep = ref(0)
  const approvalRunning = ref(false)
  const selectedDay = ref('monday')
  const meetings = computed(() => exampleMeetings.filter((meeting) => meeting.day === selectedDay.value))
  let approvalTimer

  function completeTask(id) {
    const task = tasks.value.find((item) => item.id === id)
    if (!task || task.status === 'done') return
    task.status = 'done'
    task.progress = 100
    task.deadline = '已按期完成'
  }

  function resetTasks() {
    tasks.value = initialTasks.map((task) => ({ ...task }))
  }

  function stopApproval() {
    clearInterval(approvalTimer)
    approvalRunning.value = false
  }

  function startApproval() {
    if (approvalRunning.value) return
    approvalStep.value = 0
    approvalRunning.value = true
    approvalTimer = setInterval(() => {
      approvalStep.value += 1
      if (approvalStep.value === 3) stopApproval()
    }, 1000)
  }

  watch(activePreview, stopApproval, { flush: 'sync' })
  onScopeDispose(stopApproval)

  return { activePreview, tasks, completeTask, resetTasks, approvalStep, approvalRunning, startApproval, selectedDay, meetings }
}
