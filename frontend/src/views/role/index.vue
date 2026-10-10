<template>
  <section class="role-page">
    <div class="page-heading">
      <div>
        <h1>用户与岗位角色管理</h1>
        <p>系统角色由员工职位自动确定，修改已分配账号员工的职位后会同步更新账号角色。</p>
      </div>
      <el-button :icon="Refresh" @click="fetchData">刷新</el-button>
    </div>

    <div class="role-grid">
      <section class="table-panel">
        <div class="panel-heading">
          <div>
            <h2>系统角色</h2>
            <span>总经理可查看系统内启用的岗位角色。</span>
          </div>
        </div>
        <el-table v-loading="roleLoading" :data="roles" stripe>
          <el-table-column prop="roleName" label="角色名称" min-width="130" />
          <el-table-column prop="roleCode" label="角色编码" min-width="170" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="说明" min-width="220" show-overflow-tooltip />
        </el-table>
      </section>

      <section class="table-panel">
        <div class="panel-heading">
          <div>
            <h2>职位绑定</h2>
            <span>账号角色由此映射自动生成，不能由员工档案手工指定。</span>
          </div>
        </div>
        <el-table v-loading="bindingLoading" :data="bindings" stripe>
          <el-table-column prop="positionName" label="职位" min-width="130" />
          <el-table-column prop="positionCode" label="职位编码" min-width="150" />
          <el-table-column label="对应角色" min-width="150">
            <template #default="{ row }">
              <el-tag v-if="row.roleName" type="primary">{{ row.roleName }}</el-tag>
              <el-tag v-else type="danger">未配置</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="roleCode" label="角色编码" min-width="170">
            <template #default="{ row }">{{ row.roleCode || '-' }}</template>
          </el-table-column>
        </el-table>
      </section>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { listPositionRoleBindings, listRoles } from '@/api/role'

const roleLoading = ref(false)
const bindingLoading = ref(false)
const roles = ref([])
const bindings = ref([])

onMounted(fetchData)

async function fetchData() {
  roleLoading.value = true
  bindingLoading.value = true
  try {
    const [roleResult, bindingResult] = await Promise.all([listRoles(), listPositionRoleBindings()])
    roles.value = roleResult.data || []
    bindings.value = bindingResult.data || []
  } finally {
    roleLoading.value = false
    bindingLoading.value = false
  }
}
</script>

<style scoped>
.role-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-heading,
.panel-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.page-heading h1,
.panel-heading h2 {
  margin: 0;
  color: var(--gray-900);
}

.page-heading h1 {
  font-size: 20px;
  font-weight: 650;
}

.panel-heading h2 {
  font-size: 16px;
  font-weight: 600;
}

.page-heading p,
.panel-heading span {
  display: block;
  margin: 6px 0 0;
  color: var(--gray-600);
  font-size: 13px;
  line-height: 1.6;
}

.role-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 24px;
}

.table-panel {
  padding: 20px;
  background: #fff;
  border: 1px solid var(--gray-200);
  border-radius: 8px;
}

.panel-heading {
  margin-bottom: 16px;
}

@media (max-width: 1120px) {
  .role-grid {
    grid-template-columns: 1fr;
  }
}
</style>
