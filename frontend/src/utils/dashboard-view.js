export function filterDashboardTodos(todos = [], category = 'all') {
  return todos.filter((item) => category === 'all' || item.category === category)
}

export function summarizeDashboardTodos(todos = []) {
  return todos.reduce((summary, item) => {
    summary.total += Number(item.count || 0)
    if (item.priority === 'high') {
      summary.highPriority += Number(item.count || 0)
    }
    summary.categories += 1
    return summary
  }, { total: 0, highPriority: 0, categories: 0 })
}
