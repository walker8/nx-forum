<template>
  <div class="register-container">
    <div class="register-box">
      <div class="text-2xl mb-6 text-center text-gray-500">用户注册</div>
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        @keyup.enter="handleRegister"
      >
        <el-form-item prop="account">
          <el-input
            v-model="registerForm.account"
            :placeholder="accountPlaceholder"
            :prefix-icon="registerType === 'email' ? Message : Iphone"
            clearable
          />
        </el-form-item>

        <el-form-item prop="code">
          <el-input
            v-model="registerForm.code"
            placeholder="请输入验证码"
            :prefix-icon="Key"
            clearable
          >
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

        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            type="password"
            show-password
            clearable
          />
        </el-form-item>

        <el-button type="primary" class="w-full" :loading="loading" @click="handleRegister">
          立即注册
        </el-button>

        <div class="flex justify-center mt-4">
          <span class="text-gray-500">已有账号？</span>
          <el-link type="primary" underline="hover" @click="go('/uc/login')">立即登录</el-link>
        </div>

        <div class="agreement mt-4">
          注册即表示同意
          <el-link type="primary" underline="hover" @click="go('/agreement')">《用户协议》</el-link>
          和
          <el-link type="primary" underline="hover" @click="go('/privacy')">《隐私协议》</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Iphone, Lock, Key, Message, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { sendPhoneVerifyCode, sendEmailVerifyCode } from '~/apis/uc/login'
import { register } from '~/apis/uc/user'
import { checkUsername } from '~/apis/uc/register'
import { isPhone, isEmail, isValidAccount, validateUsername } from '~/utils/validate'

const router = useRouter()
const registerFormRef = ref<FormInstance>()
const loading = ref(false)
const countdown = ref(0)

const registerForm = reactive({
  username: '',
  account: '',
  code: '',
  password: ''
})

// 在script setup部分顶部添加类型声明
type Callback = (error?: Error) => void

// 修改验证规则部分
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    {
      min: 3,
      max: 20,
      message: '用户名长度在3到20个字符',
      trigger: 'blur'
    },
    {
      validator: (_: any, value: string, callback: Callback) => {
        if (!validateUsername(value)) {
          callback(new Error('只能包含中文、字母、数字和下划线'))
          return
        }
        checkUsername(value)
          .then(() => callback())
          .catch((err) => callback(new Error(err)))
      },
      trigger: 'blur'
    }
  ],
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    {
      validator: (_: any, value: string, callback: Callback) => {
        if (registerType.value === 'phone' && !isPhone(value)) {
          callback(new Error('请输入正确的手机号'))
        } else if (registerType.value === 'email' && !isEmail(value)) {
          callback(new Error('请输入正确的邮箱'))
        } else if (registerType.value === 'both' && !isPhone(value) && !isEmail(value)) {
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
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' }
  ]
}

// 添加认证配置逻辑
const authConfig = await useAuthConfig()
const showPhoneRegister = ref(false)
const showEmailRegister = ref(false)

watchEffect(() => {
  showPhoneRegister.value = authConfig.value?.registerConfig?.enableSmsRegister ?? false
  showEmailRegister.value = authConfig.value?.registerConfig?.enableEmailRegister ?? false
  if (authConfig.value?.registerConfig?.enableRegister === false) {
    ElMessage.error('注册功能已关闭')
    router.replace('/uc/login')
  }
})

// 添加计算属性
const registerType = computed(() => {
  const config = authConfig.value?.registerConfig
  if (config?.enableSmsRegister && config?.enableEmailRegister) return 'both'
  if (config?.enableSmsRegister) return 'phone'
  if (config?.enableEmailRegister) return 'email'
  return 'none'
})

const accountPlaceholder = computed(() => {
  switch (registerType.value) {
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

// 发送验证码逻辑
const handleSendCode = async () => {
  await registerFormRef.value?.validateField('account')
  try {
    startCountdown()
    if (isPhone(registerForm.account)) {
      await sendPhoneVerifyCode(registerForm.account, 'register')
    } else {
      await sendEmailVerifyCode(registerForm.account, 'register')
    }
    ElMessage.success('验证码已发送')
  } catch (error) {
    countdown.value = 0
    ElMessage.error(error || '发送失败')
  }
}

// 注册提交
const handleRegister = async () => {
  await registerFormRef.value?.validate()
  try {
    loading.value = true
    await register({
      userName: registerForm.username,
      [isPhone(registerForm.account) ? 'phone' : 'email']: registerForm.account,
      verifyCode: registerForm.code,
      password: registerForm.password
    })
    ElMessage.success('注册成功')
    router.push('/uc/login')
  } catch (error) {
    ElMessage.error(error || '注册失败')
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

<style scoped lang="scss">
.register-container {
  @apply min-h-[calc(100vh-120px)] flex justify-center items-center p-5;
}

.register-box {
  @apply w-full max-w-[400px] bg-white rounded-lg shadow-lg p-6;
}

.agreement {
  @apply text-center text-sm text-gray-500;
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

// 移动端优化：防止点击输入框时页面自动放大
// iOS Safari 会在输入框字体小于16px时自动放大页面
@media screen and (max-width: 768px) {
  .register-box {
    padding: 20px 16px;
    
    :deep(.el-input__inner) {
      font-size: 16px !important; // 确保字体大小至少16px，防止iOS自动放大
      line-height: 1.5;
    }
  }
}
</style>
