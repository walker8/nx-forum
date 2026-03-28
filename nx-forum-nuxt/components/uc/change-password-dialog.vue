<template>
  <el-dialog
    v-model="dialogVisible"
    title="重置密码"
    :width="isMobile ? '95%' : '450px'"
    :close-on-click-modal="false"
  >
    <!-- 密码验证模式 -->
    <div v-if="mode === 'password'" class="step-content">
      <el-form :model="passwordForm" ref="passwordFormRef" :rules="passwordRules" label-width="80px">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入当前密码"
            show-password
            clearable
          />
        </el-form-item>
        <div class="mb-4 text-right">
          <el-link type="primary" :underline="false" @click="switchToVerifyCode">
            忘记密码？使用验证码验证
          </el-link>
        </div>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
            clearable
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
            clearable
          />
        </el-form-item>
      </el-form>
    </div>

    <!-- 验证码验证模式 -->
    <div v-if="mode === 'verifyCode'" class="step-content">
      <div class="mb-4">
        <el-alert type="info" :closable="false">
          <template #title>
            <div>验证码将发送至您的绑定邮箱 <el-tag type="info" size="small">{{ maskedEmail || '未绑定' }}</el-tag></div>
          </template>
        </el-alert>
      </div>

      <el-form :model="codeForm" ref="codeFormRef" :rules="codeRules" label-width="80px">
        <el-form-item label="验证码" prop="code">
          <el-input
            v-model="codeForm.code"
            placeholder="请输入6位验证码"
            maxlength="6"
            clearable
          >
            <template #append>
              <el-button
                @click="sendVerifyCode"
                :disabled="codeCountdown > 0 || !hasEmail"
                :loading="codeSending"
                style="border: none; border-radius: 0 4px 4px 0"
              >
                {{ codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <div class="mb-4 text-right">
          <el-link type="primary" :underline="false" @click="switchToPassword">
            使用密码验证
          </el-link>
        </div>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="codeForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
            clearable
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="codeForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
            clearable
          />
        </el-form-item>
      </el-form>
    </div>

    <!-- 按钮区域 -->
    <template #footer>
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">确认</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
  changePassword,
  changePasswordByCode,
  sendChangePasswordEmailCode,
} from '~/apis/uc/user'
import { useUser } from '~/composables/useUser'

const props = defineProps<{
  maskedEmail?: string
}>()

// Refs
const passwordFormRef = ref<FormInstance>()
const codeFormRef = ref<FormInstance>()

// Reactive data
const dialogVisible = ref(false)
const mode = ref<'password' | 'verifyCode'>('password')
const submitting = ref(false)
const codeSending = ref(false)
const codeCountdown = ref(0)
let countdownTimer: NodeJS.Timeout

// Password mode form
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

// Verify code mode form
const codeForm = reactive({
  code: '',
  newPassword: '',
  confirmPassword: '',
})

const hasEmail = computed(() => !!props.maskedEmail && props.maskedEmail !== '未绑定')

const isMobile = computed(() => {
  if (process.client) {
    return window.innerWidth < 768
  }
  return false
})

// Validators
const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  const newPassword = mode.value === 'password' ? passwordForm.newPassword : codeForm.newPassword
  if (value && value !== newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// Password mode rules
const passwordRules = reactive<FormRules>({
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
})

// Verify code mode rules
const codeRules = reactive<FormRules>({
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
})

// Methods
const showDialog = () => {
  resetForm()
  dialogVisible.value = true
}

const resetForm = () => {
  mode.value = 'password'
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  codeForm.code = ''
  codeForm.newPassword = ''
  codeForm.confirmPassword = ''
  submitting.value = false
  clearCountdown()
}

const clearCountdown = () => {
  codeCountdown.value = 0
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
}

const startCountdown = () => {
  codeCountdown.value = 60
  countdownTimer = setInterval(() => {
    codeCountdown.value--
    if (codeCountdown.value <= 0) {
      clearInterval(countdownTimer)
    }
  }, 1000)
}

const switchToVerifyCode = () => {
  if (!hasEmail.value) {
    ElMessage.warning('请先绑定邮箱')
    return
  }
  mode.value = 'verifyCode'
}

const switchToPassword = () => {
  mode.value = 'password'
}

const sendVerifyCode = async () => {
  try {
    codeSending.value = true
    await sendChangePasswordEmailCode()
    ElMessage.success('验证码已发送到您的邮箱')
    startCountdown()
  } catch (error: any) {
    ElMessage.error(error || '发送验证码失败')
  } finally {
    codeSending.value = false
  }
}

const handleSubmit = async () => {
  const formRef = mode.value === 'password' ? passwordFormRef.value : codeFormRef.value
  const isValid = await formRef?.validate()
  if (!isValid) return

  try {
    submitting.value = true
    if (mode.value === 'password') {
      await changePassword({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword,
      })
    } else {
      await changePasswordByCode({
        code: codeForm.code,
        newPassword: codeForm.newPassword,
      })
    }
    ElMessage.success('密码修改成功，请重新登录')
    dialogVisible.value = false
    // 清除用户状态，强制重新登录
    const user = useUser()
    user.value = {
      userId: 0,
      avatar: '',
      userName: '',
      intro: '',
      createTime: '',
      followed: false,
      lastActiveDate: ''
    }
    const token = useCookie('x_token')
    token.value = ''
    await nextTick()
    navigateTo('/uc/login')
  } catch (error: any) {
    ElMessage.error(error || '密码修改失败')
  } finally {
    submitting.value = false
  }
}

const handleCancel = () => {
  dialogVisible.value = false
  resetForm()
}

onUnmounted(() => {
  clearCountdown()
})

defineExpose({ showDialog })
</script>

<style scoped>
.step-content {
  min-height: 200px;
}

@media (max-width: 768px) {
  :deep(.el-dialog) {
    margin: 20px;
  }

  :deep(.el-form-item__label) {
    width: 70px !important;
  }

  :deep(.el-dialog__footer) {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }

  :deep(.el-dialog__footer .el-button) {
    flex: 1;
    min-width: calc(50% - 5px);
  }
}

@media (max-width: 480px) {
  :deep(.el-dialog) {
    margin: 10px;
  }

  :deep(.el-form-item__label) {
    width: 60px !important;
    font-size: 14px;
  }

  :deep(.el-dialog__footer .el-button) {
    min-width: 100%;
  }
}
</style>
