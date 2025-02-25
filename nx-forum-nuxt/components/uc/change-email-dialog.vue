<template>
  <el-dialog
    v-model="dialogVisible"
    title="修改邮箱"
    :width="isMobile ? '95%' : '450px'"
    :close-on-click-modal="false"
  >
    <!-- 步骤条 -->
    <el-steps :active="currentStep - 1" finish-status="success" class="mb-6">
      <el-step title="验证当前邮箱" />
      <el-step title="绑定新邮箱" />
    </el-steps>

    <!-- 第一步：验证当前邮箱 -->
    <div v-if="currentStep === 1" class="step-content">
      <el-alert type="info" :closable="false" class="mb-4">
        <template #title>
          <div>为了确保账户安全，我们将向您当前绑定的邮箱发送验证码</div>
          <div class="mt-2">
            <el-tag type="info" size="small">当前邮箱：{{ maskedCurrentEmail }}</el-tag>
          </div>
        </template>
      </el-alert>

      <el-form
        class="mt-4"
        :model="currentEmailForm"
        ref="currentEmailFormRef"
        :rules="currentEmailRules"
      >
        <el-form-item prop="verifyCode">
          <el-input
            v-model="currentEmailForm.verifyCode"
            placeholder="请输入6位验证码"
            maxlength="6"
            clearable
          >
            <template #append>
              <el-button
                @click="sendCurrentEmailCode"
                :disabled="currentEmailCodeCountdown > 0"
                :loading="currentEmailCodeSending"
                style="border: none; border-radius: 0 4px 4px 0"
              >
                {{ currentEmailCodeCountdown > 0 ? `${currentEmailCodeCountdown}s` : '发送验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
    </div>

    <!-- 第二步：设置新邮箱 -->
    <div v-if="currentStep === 2" class="step-content">
      <el-alert type="success" :closable="false" show-icon class="mb-4">
        <template #title> 请输入新的邮箱地址，我们将向新邮箱发送验证码 </template>
      </el-alert>

      <el-form class="mt-4" :model="newEmailForm" ref="newEmailFormRef" :rules="newEmailRules">
        <el-form-item prop="newEmail">
          <el-input
            v-model="newEmailForm.newEmail"
            placeholder="请输入新的邮箱地址"
            type="email"
            clearable
          />
        </el-form-item>
        <el-form-item prop="verifyCode">
          <el-input
            v-model="newEmailForm.verifyCode"
            placeholder="请输入6位验证码"
            maxlength="6"
            clearable
          >
            <template #append>
              <el-button
                @click="sendNewEmailCode"
                :disabled="newEmailCodeCountdown > 0 || !newEmailForm.newEmail"
                :loading="newEmailCodeSending"
                style="border: none; border-radius: 0 4px 4px 0"
              >
                {{ newEmailCodeCountdown > 0 ? `${newEmailCodeCountdown}s` : '发送验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
    </div>

    <!-- 按钮区域 -->
    <template #footer>
      <el-button @click="handleCancel">取消</el-button>
      <el-button v-if="currentStep === 2" @click="handleNewEmailBack">上一步</el-button>
      <el-button
        v-if="currentStep === 1"
        type="primary"
        @click="handleCurrentEmailNext"
        :loading="currentEmailLoading"
      >
        下一步
      </el-button>
      <el-button
        v-if="currentStep === 2"
        type="primary"
        @click="handleNewEmailComplete"
        :loading="newEmailLoading"
      >
        完成换绑
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
  changeToNewEmail,
  sendCurrentUserEmailVerifyCode,
  sendNewEmailVerifyCode,
  verifyCurrentEmail,
  type ChangeToNewEmailCmd,
  type VerifyCurrentEmailCmd
} from '~/apis/uc/user'

// Props & Emits
const props = defineProps<{
  currentEmail?: string
}>()

const emit = defineEmits<{
  success: []
}>()

// Refs
const currentEmailFormRef = ref<FormInstance>()
const newEmailFormRef = ref<FormInstance>()

// Reactive data
const dialogVisible = ref(false)
const currentStep = ref(1)

// 第一步表单
const currentEmailForm = reactive<VerifyCurrentEmailCmd>({
  verifyCode: ''
})

// 第二步表单
const newEmailForm = reactive<ChangeToNewEmailCmd>({
  newEmail: '',
  verifyCode: ''
})

// 加载状态
const currentEmailLoading = ref(false)
const newEmailLoading = ref(false)
const currentEmailCodeSending = ref(false)
const newEmailCodeSending = ref(false)

// 倒计时
const currentEmailCodeCountdown = ref(0)
const newEmailCodeCountdown = ref(0)
let currentEmailCountdownTimer: NodeJS.Timeout
let newEmailCountdownTimer: NodeJS.Timeout

// 计算属性
const isMobile = computed(() => {
  if (process.client) {
    return window.innerWidth < 768
  }
  return false
})

const maskedCurrentEmail = computed(() => {
  if (!props.currentEmail) return '***@***.***'
  const [username, domain] = props.currentEmail.split('@')
  const maskedUsername = username.substring(0, 2) + '*'.repeat(Math.max(0, username.length - 2))
  return `${maskedUsername}@${domain}`
})

// 表单验证规则
const currentEmailRules = reactive<FormRules>({
  verifyCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ]
})

const newEmailRules = reactive<FormRules>({
  newEmail: [
    { required: true, message: '请输入新邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  verifyCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ]
})

// 方法
const showDialog = () => {
  resetForm()
  dialogVisible.value = true
}

const resetForm = () => {
  currentStep.value = 1
  currentEmailForm.verifyCode = ''
  newEmailForm.newEmail = ''
  newEmailForm.verifyCode = ''
  currentEmailLoading.value = false
  newEmailLoading.value = false
  clearCountdowns()
}

const clearCountdowns = () => {
  currentEmailCodeCountdown.value = 0
  newEmailCodeCountdown.value = 0
  clearInterval(currentEmailCountdownTimer)
  clearInterval(newEmailCountdownTimer)
}

const startCountdown = (type: 'current' | 'new') => {
  if (type === 'current') {
    currentEmailCodeCountdown.value = 60
    currentEmailCountdownTimer = setInterval(() => {
      currentEmailCodeCountdown.value--
      if (currentEmailCodeCountdown.value <= 0) {
        clearInterval(currentEmailCountdownTimer)
      }
    }, 1000)
  } else {
    newEmailCodeCountdown.value = 60
    newEmailCountdownTimer = setInterval(() => {
      newEmailCodeCountdown.value--
      if (newEmailCodeCountdown.value <= 0) {
        clearInterval(newEmailCountdownTimer)
      }
    }, 1000)
  }
}

const sendCurrentEmailCode = async () => {
  try {
    currentEmailCodeSending.value = true
    await sendCurrentUserEmailVerifyCode()
    ElMessage.success('验证码已发送到您的邮箱')
    startCountdown('current')
  } catch (error: any) {
    ElMessage.error(error || '发送验证码失败')
  } finally {
    currentEmailCodeSending.value = false
  }
}

const sendNewEmailCode = async () => {
  if (!newEmailForm.newEmail) {
    ElMessage.warning('请先输入新邮箱地址')
    return
  }

  try {
    newEmailCodeSending.value = true
    await sendNewEmailVerifyCode(newEmailForm.newEmail)
    ElMessage.success('验证码已发送到新邮箱')
    startCountdown('new')
  } catch (error: any) {
    ElMessage.error(error || '发送验证码失败')
  } finally {
    newEmailCodeSending.value = false
  }
}

const handleCurrentEmailNext = async () => {
  const isValid = await currentEmailFormRef.value?.validate()
  if (!isValid) return

  try {
    currentEmailLoading.value = true
    await verifyCurrentEmail(currentEmailForm)
    ElMessage.success('当前邮箱验证成功')
    currentStep.value = 2
  } catch (error: any) {
    ElMessage.error(error || '验证失败')
  } finally {
    currentEmailLoading.value = false
  }
}

const handleNewEmailBack = () => {
  currentStep.value = 1
  newEmailForm.newEmail = ''
  newEmailForm.verifyCode = ''
}

const handleNewEmailComplete = async () => {
  const isValid = await newEmailFormRef.value?.validate()
  if (!isValid) return

  try {
    newEmailLoading.value = true
    await changeToNewEmail(newEmailForm)
    ElMessage.success('邮箱换绑成功')
    dialogVisible.value = false
    emit('success')
  } catch (error: any) {
    ElMessage.error(error || '换绑失败')
  } finally {
    newEmailLoading.value = false
  }
}

const handleCancel = () => {
  dialogVisible.value = false
  resetForm()
}

// 生命周期
onUnmounted(() => {
  clearCountdowns()
})

// 暴露方法
defineExpose({ showDialog })
</script>

<style scoped>
.step-content {
  min-height: 150px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  :deep(.el-dialog) {
    margin: 20px;
  }

  :deep(.el-steps) {
    margin-bottom: 20px;
  }

  :deep(.el-step__title) {
    font-size: 13px;
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

  :deep(.el-step__title) {
    font-size: 12px;
  }

  :deep(.el-dialog__footer .el-button) {
    min-width: 100%;
  }
}
</style>
