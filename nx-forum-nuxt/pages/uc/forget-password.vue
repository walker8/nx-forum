<template>
  <div class="forget-container">
    <div class="forget-box">
      <div class="text-2xl mb-6 text-center text-gray-500">找回密码</div>

      <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="handleSubmit">
        <el-form-item prop="account">
          <el-input
            v-model="form.account"
            :placeholder="accountPlaceholder"
            :prefix-icon="resetType === 'email' ? Message : Iphone"
            clearable
          />
        </el-form-item>

        <el-form-item prop="code">
          <el-input v-model="form.code" placeholder="请输入验证码" :prefix-icon="Key" clearable>
            <template #suffix>
              <el-button
                link
                :disabled="!!countdown"
                @click.stop="handleSendCode"
                class="send-code-btn"
              >
                {{ countdown ? `${countdown}s后重发` : '获取验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            placeholder="请输入新密码"
            :prefix-icon="Lock"
            type="password"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            placeholder="请确认新密码"
            :prefix-icon="Lock"
            type="password"
            show-password
            clearable
          />
        </el-form-item>

        <el-button type="primary" class="w-full" :loading="loading" @click="handleSubmit">
          重置密码
        </el-button>

        <div class="flex justify-center mt-4">
          <el-link type="primary" underline="hover" @click="go('/uc/login')">返回登录</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Iphone, Key, Lock, Message } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import {
  resetPasswordByVerifyCode,
  sendEmailVerifyCode,
  sendPhoneVerifyCode
} from '~/apis/uc/login'
import { isEmail, isPhone } from '~/utils/validate'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const countdown = ref(0)

const form = reactive({
  account: '',
  code: '',
  password: '',
  confirmPassword: ''
})

type Callback = (error?: Error) => void

// 验证规则
const validatePassConfirm = (rule: any, value: string, callback: any) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}
const rules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    {
      validator: (_: any, value: string, callback: Callback) => {
        if (resetType.value === 'phone' && !isPhone(value)) {
          callback(new Error('请输入正确的手机号'))
        } else if (resetType.value === 'email' && !isEmail(value)) {
          callback(new Error('请输入正确的邮箱'))
        } else if (resetType.value === 'both' && !isPhone(value) && !isEmail(value)) {
          callback(new Error('请输入正确的手机号或邮箱'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度为6位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validatePassConfirm, trigger: 'blur' }
  ]
}

// 添加认证配置逻辑
const authConfig = await useAuthConfig()
const showPhoneReset = ref(false)
const showEmailReset = ref(false)

watchEffect(() => {
  showPhoneReset.value = authConfig.value?.loginConfig?.enablePhoneResetPassword ?? false
  showEmailReset.value = authConfig.value?.loginConfig?.enableEmailResetPassword ?? false
})

// 添加计算属性
const resetType = computed(() => {
  const config = authConfig.value?.loginConfig
  if (config?.enablePhoneResetPassword && config?.enableEmailResetPassword) return 'both'
  if (config?.enablePhoneResetPassword) return 'phone'
  if (config?.enableEmailResetPassword) return 'email'
  return 'none'
})

const accountPlaceholder = computed(() => {
  switch (resetType.value) {
    case 'phone':
      return '请输入手机号'
    case 'email':
      return '请输入邮箱'
    case 'both':
      return '请输入手机号/邮箱'
    default:
      return ''
  }
})

// 发送验证码
const handleSendCode = async () => {
  await formRef.value?.validateField('account')
  try {
    // 提前启动倒计时，禁用按钮
    startCountdown()

    if (isPhone(form.account)) {
      if (!authConfig.value?.loginConfig?.enablePhoneResetPassword) {
        throw new Error('手机找回密码功能未开启')
      }
      await sendPhoneVerifyCode(form.account, 'reset_password')
    } else {
      if (!authConfig.value?.loginConfig?.enableEmailResetPassword) {
        throw new Error('邮箱找回密码功能未开启')
      }
      await sendEmailVerifyCode(form.account, 'reset_password')
    }

    ElMessage.success('验证码已发送')
  } catch (error) {
    // 如果发送失败，重置倒计时
    countdown.value = 0
    ElMessage.error(error || '发送失败')
  }
}

// 提交重置
const handleSubmit = async () => {
  await formRef.value?.validate()
  try {
    loading.value = true
    await resetPasswordByVerifyCode({
      account: form.account,
      code: form.code,
      newPassword: form.password,
      type: isPhone(form.account) ? 'phone' : 'email'
    })
    ElMessage.success('密码重置成功')
    router.push('/user/login')
  } catch (error) {
    ElMessage.error(error || '重置失败')
  } finally {
    loading.value = false
  }
}

// 公共方法
const startCountdown = () => {
  countdown.value = 60
  const timer = setInterval(() => {
    if (countdown.value <= 0) clearInterval(timer)
    else countdown.value--
  }, 1000)
}
const go = (path: string) => router.push(path)
</script>

<style scoped>
.forget-container {
  @apply min-h-[calc(100vh-120px)] flex justify-center items-center p-5;
}

.forget-box {
  @apply w-full max-w-[400px] bg-white rounded-lg shadow-lg p-6;
}

:deep(.send-code-btn) {
  margin-right: -10px;
  color: var(--el-color-primary);
  padding: 0 8px;
  &:hover {
    background: transparent;
    color: var(--el-color-primary-light-3);
  }
  &.is-disabled {
    color: var(--el-text-color-placeholder);
  }
}
</style>
