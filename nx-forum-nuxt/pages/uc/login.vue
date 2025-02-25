<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-tabs" v-if="showCodeLogin || showAccountLogin">
        <div
          v-if="showCodeLogin"
          :class="['tab-item', activeTab === 'code' ? 'active' : '']"
          @click="activeTab = 'code'"
        >
          验证码登录
        </div>
        <div
          v-if="showAccountLogin"
          :class="['tab-item', activeTab === 'account' ? 'active' : '']"
          @click="activeTab = 'account'"
        >
          账号密码登录
        </div>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <!-- 账号密码登录 -->
        <template v-if="activeTab === 'account' && showAccountLogin">
          <el-form-item prop="account">
            <el-input
              v-model="loginForm.account"
              placeholder="请输入手机号/邮箱/账号"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              :type="showPassword ? 'text' : 'password'"
              clearable
            >
              <template #suffix>
                <el-icon class="cursor-pointer" @click="showPassword = !showPassword">
                  <View v-if="showPassword" />
                  <Hide v-else />
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
        </template>

        <!-- 验证码登录 -->
        <template v-if="activeTab === 'code' && showCodeLogin">
          <el-form-item prop="account">
            <el-input
              v-model="loginForm.account"
              :placeholder="codeLoginPlaceholder"
              :prefix-icon="codeLoginType === 'email' ? Message : Iphone"
              clearable
            />
          </el-form-item>
          <el-form-item prop="code">
            <el-input
              v-model="loginForm.code"
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
        </template>

        <div class="flex justify-end mb-4">
          <el-link type="primary" underline="hover" @click="go('/uc/forget-password')">忘记密码？</el-link>
        </div>

        <el-button type="primary" class="w-full" :loading="loading" @click="handleLogin">
          登录
        </el-button>

        <div class="flex justify-between mt-4">
          <span class="text-gray-500 text-sm"></span>
          <el-link v-if="showRegister" type="primary" underline="hover" @click="go('/uc/register')">立即注册</el-link>
        </div>

        <div class="third-party-login">
          <div class="divider">
            <span>其他登录方式</span>
          </div>
          <div class="login-icons">
            <!-- <img
              src="/images/wechat.png"
              alt="微信登录"
              class="cursor-pointer"
              @click="handleThirdPartyLogin('wechat')"
            />
            <img
              src="/images/qq.png"
              alt="QQ登录"
              class="cursor-pointer"
              @click="handleThirdPartyLogin('qq')"
            />
            <img
              src="/images/weibo.png"
              alt="微博登录"
              class="cursor-pointer"
              @click="handleThirdPartyLogin('weibo')"
            /> -->
          </div>
        </div>

        <div class="agreement">
          注册登录即表示同意
          <el-link type="primary" underline="hover" @click="go('/agreement')">《用户协议》</el-link>
          和
          <el-link type="primary" underline="hover" @click="go('/privacy')">《隐私协议》</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { User, Lock, View, Hide, Message, Iphone, Key } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { login, verifyCodeLogin, sendPhoneVerifyCode, sendEmailVerifyCode } from '~/apis/uc/login'
import { useGlobalStore } from '~/stores/global.store'
import { isPhone, isEmail, isValidAccount } from '~/utils/validate'
const router = useRouter()
const { user } = useCurrentUser()

const loginFormRef = ref<FormInstance>()
const activeTab = ref('account')
const showPassword = ref(false)
const loading = ref(false)
const countdown = ref(0)
const global = useGlobalStore()
const showRegister = ref(true)

// 登录表单
const loginForm = reactive({
  account: '',
  password: '',
  code: ''
})

const codeLoginType = computed(() => {
  const config = authConfig.value?.loginConfig
  if (config?.enablePhoneCodeLogin && config?.enableEmailCodeLogin) {
    return 'both'
  }
  if (config?.enablePhoneCodeLogin) return 'phone'
  if (config?.enableEmailCodeLogin) return 'email'
  return 'none'
})

const codeLoginPlaceholder = computed(() => {
  switch (codeLoginType.value) {
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

// 表单验证规则
const loginRules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (!value) return callback(new Error('请输入账号'))
        if (activeTab.value === 'code') {
          if (codeLoginType.value === 'phone' && !isPhone(value)) {
            callback(new Error('请输入正确的手机号'))
          } else if (codeLoginType.value === 'email' && !isEmail(value)) {
            callback(new Error('请输入正确的邮箱'))
          } else if (codeLoginType.value === 'both' && !isPhone(value) && !isEmail(value)) {
            callback(new Error('请输入正确的手机号或邮箱'))
          } else {
            callback()
          }
        } else {
          if (!isValidAccount(value) && !validateUsername(value)) {
            callback(new Error('请输入正确的账号'))
          } else {
            callback()
          }
        }
      },
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度为6位', trigger: 'blur' }
  ]
}

// 添加认证配置逻辑
const authConfig = await useAuthConfig()
const showCodeLogin = ref(true)
const showAccountLogin = ref(true)

watchEffect(() => {
  showCodeLogin.value =
    authConfig.value?.loginConfig?.enablePhoneCodeLogin ||
    authConfig.value?.loginConfig?.enableEmailCodeLogin ||
    false
  showAccountLogin.value = authConfig.value?.loginConfig?.enablePasswordLogin ?? true

  // 设置默认激活的tab
  const availableTabs = []
  if (showCodeLogin.value) availableTabs.push('code')
  if (showAccountLogin.value) availableTabs.push('account')

  if (availableTabs.length === 0) {
    ElMessage.error('当前没有可用的登录方式')
    return
  }

  // 优先保持原有tab值，如果不可用则取第一个可用tab
  activeTab.value = availableTabs.includes(activeTab.value) ? activeTab.value : availableTabs[0]

  // 控制注册入口显示
  showRegister.value = authConfig.value?.registerConfig?.enableRegister ?? true
})

// 发送验证码
const handleSendCode = async () => {
  loginFormRef.value?.validateField('account').then(async () => {
    const config = authConfig.value?.loginConfig

    try {
      if (isPhone(loginForm.account)) {
        if (!config?.enablePhoneCodeLogin) {
          throw new Error('手机验证码登录未开启')
        }
        await sendPhoneVerifyCode(loginForm.account)
      } else {
        if (!config?.enableEmailCodeLogin) {
          throw new Error('邮箱验证码登录未开启')
        }
        await sendEmailVerifyCode(loginForm.account)
      }

      // 启动倒计时
      countdown.value = 60
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) clearInterval(timer)
      }, 1000)
      ElMessage.success('验证码已发送')
    } catch (error: any) {
      ElMessage.error(error || '发送失败')
    }
  })
}

// 处理登录
const handleLogin = async () => {
  loginFormRef.value?.validate().then(async () => {
    try {
      loading.value = true

      let res
      if (activeTab.value === 'account') {
        res = await login({
          userName: loginForm.account,
          password: loginForm.password
        })
      } else {
        // 添加类型校验
        const type = isEmail(loginForm.account) ? 'email' : 'phone'
        if (type === 'email' && !authConfig.value?.loginConfig?.enableEmailCodeLogin) {
          throw new Error('邮箱验证码登录未启用')
        }
        if (type === 'phone' && !authConfig.value?.loginConfig?.enablePhoneCodeLogin) {
          throw new Error('手机验证码登录未启用')
        }

        res = await verifyCodeLogin({
          account: loginForm.account,
          code: loginForm.code,
          type: isEmail(loginForm.account) ? 'email' : 'phone'
        })
      }

      const token = useCookie('x_token', {
        expires: new Date(res.data.tokenExpiresIn),
        httpOnly: global.httpOnly
      })
      token.value = res.data.token
      // 清除用户权限
      const { refreshUserAuth, authPromise } = useUserAuth()
      await authPromise
      refreshUserAuth()
      ElMessage.success('登录成功')
      router.push('/')
    } catch (error: any) {
      ElMessage.error(error || '登录失败')
    } finally {
      loading.value = false
    }
  })
}

// 第三方登录
const handleThirdPartyLogin = (type: string) => {
  ElMessage.info(`暂不支持${type}登录`)
}

// 页面跳转
const go = (path: string) => {
  router.push(path)
}
</script>

<style scoped lang="scss">
.login-container {
  min-height: calc(100vh - 120px);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.login-box {
  width: 100%;
  max-width: 400px;
  background-color: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.login-tabs {
  display: flex;
  margin-bottom: 24px;
  border-bottom: 1px solid #e4e4e4;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  cursor: pointer;
  color: #666;
  position: relative;
}

.tab-item.active {
  color: var(--el-color-primary);
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: var(--el-color-primary);
}

.third-party-login {
  margin-top: 24px;
}

.divider {
  display: flex;
  align-items: center;
  color: #999;
  margin: 20px 0;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background-color: #e4e4e4;
}

.divider span {
  padding: 0 12px;
}

.login-icons {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-top: 20px;
}

.login-icons img {
  width: 32px;
  height: 32px;
  transition: transform 0.2s;
}

.login-icons img:hover {
  transform: scale(1.1);
}

@media (max-width: 640px) {
  .login-box {
    padding: 20px;
  }

  .login-icons {
    gap: 20px;
  }
}

.agreement {
  margin-top: 20px;
  text-align: center;
  color: rgb(138, 145, 159);
  font-size: 14px;
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
  .login-form {
    :deep(.el-input__inner) {
      font-size: 16px !important; // 确保字体大小至少16px，防止iOS自动放大
      line-height: 1.5;
    }
  }

  .login-box {
    padding: 20px 16px;
  }

  .login-tabs {
    margin-bottom: 20px;
  }

  .tab-item {
    padding: 10px 0;
    font-size: 15px;
  }
}
</style>
