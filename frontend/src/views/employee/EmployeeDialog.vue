<template>
  <el-dialog
    :model-value="modelValue"
    :title="isEdit ? '&#x7f16;&#x8f91;&#x5458;&#x5de5;' : '&#x65b0;&#x589e;&#x5458;&#x5de5;'"
    width="760px"
    destroy-on-close
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="104px">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="&#x5458;&#x5de5;&#x5de5;&#x53f7;" prop="employeeNo">
            <el-input v-model.trim="form.employeeNo" placeholder="&#x8bf7;&#x8f93;&#x5165;&#x5458;&#x5de5;&#x5de5;&#x53f7;" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="&#x5458;&#x5de5;&#x59d3;&#x540d;" prop="employeeName">
            <el-input v-model.trim="form.employeeName" placeholder="&#x8bf7;&#x8f93;&#x5165;&#x5458;&#x5de5;&#x59d3;&#x540d;" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="&#x6240;&#x5c5e;&#x90e8;&#x95e8;" prop="deptId">
            <el-select v-model="form.deptId" placeholder="&#x8bf7;&#x9009;&#x62e9;&#x90e8;&#x95e8;" clearable @change="handleDeptChange">
              <el-option v-for="item in departmentOptions" :key="item.id" :label="item.deptName" :value="item.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="&#x804c;&#x4f4d;" prop="positionId">
            <el-select v-model="form.positionId" placeholder="&#x8bf7;&#x9009;&#x62e9;&#x804c;&#x4f4d;" clearable>
              <el-option v-for="item in availablePositions" :key="item.id" :label="item.positionName" :value="item.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="&#x6027;&#x522b;" prop="gender">
            <el-radio-group v-model="form.gender">
              <el-radio-button :label="0">&#x672a;&#x77e5;</el-radio-button>
              <el-radio-button :label="1">&#x7537;</el-radio-button>
              <el-radio-button :label="2">&#x5973;</el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="&#x5458;&#x5de5;&#x72b6;&#x6001;" prop="employmentStatus">
            <el-select v-model="form.employmentStatus" placeholder="&#x8bf7;&#x9009;&#x62e9;&#x72b6;&#x6001;">
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="&#x624b;&#x673a;&#x53f7;" prop="phone">
            <el-input v-model.trim="form.phone" placeholder="&#x8bf7;&#x8f93;&#x5165;&#x624b;&#x673a;&#x53f7;" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="&#x90ae;&#x7bb1;" prop="email">
            <el-input v-model.trim="form.email" placeholder="&#x8bf7;&#x8f93;&#x5165;&#x90ae;&#x7bb1;" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="&#x5165;&#x804c;&#x65e5;&#x671f;" prop="hireDate">
            <el-date-picker v-model="form.hireDate" type="date" value-format="YYYY-MM-DD" placeholder="&#x8bf7;&#x9009;&#x62e9;&#x65e5;&#x671f;" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="&#x51fa;&#x751f;&#x65e5;&#x671f;" prop="birthday">
            <el-date-picker v-model="form.birthday" type="date" value-format="YYYY-MM-DD" placeholder="&#x8bf7;&#x9009;&#x62e9;&#x65e5;&#x671f;" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="&#x8f6c;&#x6b63;&#x65e5;&#x671f;" prop="regularDate">
            <el-date-picker v-model="form.regularDate" type="date" value-format="YYYY-MM-DD" placeholder="&#x8bf7;&#x9009;&#x62e9;&#x65e5;&#x671f;" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="&#x79bb;&#x804c;&#x65e5;&#x671f;" prop="resignDate">
            <el-date-picker v-model="form.resignDate" type="date" value-format="YYYY-MM-DD" placeholder="&#x8bf7;&#x9009;&#x62e9;&#x65e5;&#x671f;" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="&#x8eab;&#x4efd;&#x8bc1;&#x53f7;" prop="idCard">
            <el-input v-model.trim="form.idCard" placeholder="&#x8bf7;&#x8f93;&#x5165;&#x8eab;&#x4efd;&#x8bc1;&#x53f7;" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="&#x5c45;&#x4f4f;&#x5730;&#x5740;" prop="address">
            <el-input v-model.trim="form.address" placeholder="&#x8bf7;&#x8f93;&#x5165;&#x5c45;&#x4f4f;&#x5730;&#x5740;" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="&#x7d27;&#x6025;&#x8054;&#x7cfb;&#x4eba;" prop="emergencyContact">
            <el-input v-model.trim="form.emergencyContact" placeholder="&#x8bf7;&#x8f93;&#x5165;&#x8054;&#x7cfb;&#x4eba;" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="&#x7d27;&#x6025;&#x7535;&#x8bdd;" prop="emergencyPhone">
            <el-input v-model.trim="form.emergencyPhone" placeholder="&#x8bf7;&#x8f93;&#x5165;&#x7d27;&#x6025;&#x7535;&#x8bdd;" />
          </el-form-item>
        </el-col>

        <template v-if="!isEdit">
          <el-col :span="24">
            <el-divider content-position="left">&#x8d26;&#x53f7;&#x5206;&#x914d;&#xff08;&#x53ef;&#x9009;&#xff09;</el-divider>
          </el-col>
          <el-col :span="12">
            <el-form-item label="&#x767b;&#x5f55;&#x8d26;&#x53f7;" prop="username">
              <el-input v-model.trim="form.username" placeholder="&#x4e0d;&#x586b;&#x5219;&#x6682;&#x4e0d;&#x5206;&#x914d;&#x8d26;&#x53f7;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="&#x521d;&#x59cb;&#x5bc6;&#x7801;" prop="password">
              <el-input v-model="form.password" type="password" show-password placeholder="&#x5206;&#x914d;&#x8d26;&#x53f7;&#x65f6;&#x5fc5;&#x586b;" />
            </el-form-item>
          </el-col>
        </template>

        <el-col :span="24">
          <el-form-item label="&#x5907;&#x6ce8;" prop="remark">
            <el-input v-model.trim="form.remark" type="textarea" :rows="3" placeholder="&#x8bf7;&#x8f93;&#x5165;&#x5907;&#x6ce8;" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">&#x53d6;&#x6d88;</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">&#x4fdd;&#x5b58;</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createEmployee, updateEmployee } from '@/api/employee'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  employee: {
    type: Object,
    default: null
  },
  departmentOptions: {
    type: Array,
    default: () => []
  },
  positionOptions: {
    type: Array,
    default: () => []
  },
  statusOptions: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const emptyForm = {
  id: null,
  employeeNo: '',
  deptId: null,
  positionId: null,
  employeeName: '',
  gender: 0,
  birthday: '',
  idCard: '',
  phone: '',
  email: '',
  hireDate: '',
  regularDate: '',
  resignDate: '',
  employmentStatus: 1,
  address: '',
  emergencyContact: '',
  emergencyPhone: '',
  remark: '',
  username: '',
  password: ''
}

const formRef = ref()
const submitting = ref(false)
const form = reactive({ ...emptyForm })
const isEdit = computed(() => Boolean(form.id))
const availablePositions = computed(() =>
  props.positionOptions.filter((item) => !item.deptId || item.deptId === form.deptId)
)

const rules = {
  employeeNo: [{ required: true, message: '\u8bf7\u8f93\u5165\u5458\u5de5\u5de5\u53f7', trigger: 'blur' }],
  employeeName: [{ required: true, message: '\u8bf7\u8f93\u5165\u5458\u5de5\u59d3\u540d', trigger: 'blur' }],
  deptId: [{ required: true, message: '\u8bf7\u9009\u62e9\u90e8\u95e8', trigger: 'change' }],
  employmentStatus: [{ required: true, message: '\u8bf7\u9009\u62e9\u5458\u5de5\u72b6\u6001', trigger: 'change' }],
  email: [{ type: 'email', message: '\u90ae\u7bb1\u683c\u5f0f\u4e0d\u6b63\u786e', trigger: 'blur' }],
  password: [
    {
      validator: (_, value, callback) => {
        if (!isEdit.value && form.username && !value) {
          callback(new Error('\u5206\u914d\u8d26\u53f7\u65f6\u5bc6\u7801\u4e0d\u80fd\u4e3a\u7a7a'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

watch(
  () => [props.modelValue, props.employee],
  ([visible, employee]) => {
    if (!visible) {
      return
    }
    Object.assign(form, emptyForm, employee || {})
    form.gender = form.gender ?? 0
    form.employmentStatus = form.employmentStatus ?? 1
  },
  { immediate: true }
)

function handleDeptChange() {
  const selected = props.positionOptions.find((item) => item.id === form.positionId)
  if (selected && selected.deptId && selected.deptId !== form.deptId) {
    form.positionId = null
  }
}

function buildPayload() {
  const payload = { ...form }
  if (isEdit.value) {
    delete payload.username
    delete payload.password
  }
  if (!payload.username) {
    delete payload.username
    delete payload.password
  }
  return payload
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateEmployee(buildPayload())
      ElMessage.success('\u5458\u5de5\u4fe1\u606f\u5df2\u66f4\u65b0')
    } else {
      await createEmployee(buildPayload())
      ElMessage.success('\u5458\u5de5\u5df2\u65b0\u589e')
    }
    emit('success')
    handleClose()
  } finally {
    submitting.value = false
  }
}

function handleClose() {
  emit('update:modelValue', false)
}
</script>

<style scoped>
.el-select,
.el-date-editor {
  width: 100%;
}
</style>
