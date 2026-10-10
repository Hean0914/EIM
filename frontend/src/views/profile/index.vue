<template>
  <section class="profile-page">
    <div class="profile-panel">
      <h1>个人信息</h1>
      <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="96px">
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model.trim="profileForm.realName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model.trim="profileForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model.trim="profileForm.email" />
        </el-form-item>
        <el-form-item label="头像">
          <div class="avatar-field">
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :disabled="avatarUploading"
              accept="image/jpeg,image/png,image/gif,image/webp"
              :before-upload="beforeAvatarUpload"
              :http-request="handleAvatarUpload"
            >
              <el-avatar v-if="profileForm.avatar" :size="72" :src="profileForm.avatar" fit="cover" />
              <div v-else class="avatar-placeholder">
                <el-icon><Plus /></el-icon>
              </div>
            </el-upload>
            <div class="avatar-tips">
              <span>支持 jpg/jpeg/png/gif/webp，单个文件不超过 5MB</span>
              <span v-if="profileForm.avatar">点击头像可重新上传，保存信息后生效</span>
              <el-button v-if="profileForm.avatar" link type="danger" @click="handleClearAvatar">清除头像</el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="profileLoading" @click="handleUpdateProfile">保存信息</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="profile-panel">
      <h1>修改密码</h1>
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="96px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="passwordLoading" @click="handleChangePassword">修改密码</el-button>
        </el-form-item>
      </el-form>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { changePassword, updateProfile } from '@/api/auth'
import { uploadImage } from '@/api/file'
import { useUserStore } from '@/store/user'

const ALLOWED_AVATAR_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
const MAX_AVATAR_SIZE = 5 * 1024 * 1024

const userStore = useUserStore()
const profileFormRef = ref()
const passwordFormRef = ref()
const profileLoading = ref(false)
const passwordLoading = ref(false)
const avatarUploading = ref(false)

const profileForm = reactive({
  realName: userStore.user?.realName || '',
  phone: userStore.user?.phone || '',
  email: userStore.user?.email || '',
  avatar: userStore.user?.avatar || ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: ''
})

const profileRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }]
}

function beforeAvatarUpload(file) {
  if (!ALLOWED_AVATAR_TYPES.includes(file.type)) {
    ElMessage.error('仅支持 jpg/jpeg/png/gif/webp 格式的图片')
    return false
  }
  if (file.size > MAX_AVATAR_SIZE) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

// el-upload 默认的 action 不经过 axios 实例，会缺少 Authorization 头而被后端拦截，因此使用自定义上传
async function handleAvatarUpload(options) {
  avatarUploading.value = true
  try {
    const result = await uploadImage(options.file)
    profileForm.avatar = result.data
    options.onSuccess(result)
    ElMessage.success('头像上传成功，点击「保存信息」后生效')
  } catch (error) {
    options.onError(error)
  } finally {
    avatarUploading.value = false
  }
}

function handleClearAvatar() {
  profileForm.avatar = ''
}

async function handleUpdateProfile() {
  const valid = await profileFormRef.value.validate().catch(() => false)
  if (!valid) return
  profileLoading.value = true
  try {
    await updateProfile({ ...profileForm })
    userStore.setLoginInfo(userStore.token, { ...userStore.user, ...profileForm })
    ElMessage.success('个人信息已更新')
  } finally {
    profileLoading.value = false
  }
}

async function handleChangePassword() {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return
  passwordLoading.value = true
  try {
    await changePassword({ ...passwordForm })
    Object.assign(passwordForm, { oldPassword: '', newPassword: '' })
    ElMessage.success('密码已修改')
  } finally {
    passwordLoading.value = false
  }
}
</script>

<style scoped>
.profile-page {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 16px;
}

.profile-panel {
  padding: 22px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.profile-panel h1 {
  margin: 0 0 20px;
  font-size: 20px;
}

.avatar-field {
  display: flex;
  align-items: center;
  gap: 14px;
}

.avatar-uploader {
  cursor: pointer;
  border-radius: 50%;
}

.avatar-placeholder {
  display: grid;
  width: 72px;
  height: 72px;
  color: #9ca3af;
  background: #f9fafb;
  border: 1px dashed #d1d5db;
  border-radius: 50%;
  font-size: 22px;
  place-items: center;
}

.avatar-uploader:hover .avatar-placeholder {
  color: var(--color-brand);
  border-color: var(--color-brand);
}

.avatar-tips {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  color: #6b7280;
  font-size: 12px;
  line-height: 1.5;
}

@media (max-width: 900px) {
  .profile-page {
    grid-template-columns: 1fr;
  }
}
</style>
