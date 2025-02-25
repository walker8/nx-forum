<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type { LoginConfigDTO } from '~/apis/uc/login'
import { getLoginConfig, updateLoginConfig } from '~/apis/uc/login'

definePageMeta({
  layout: 'uc'
})

const loading = ref(false)
const formData = ref<LoginConfigDTO>({
  enablePhoneCodeLogin: false,
  enableEmailCodeLogin: false,
  enablePasswordLogin: true,
  enableLoginCaptcha: false,
  maxLoginFailCount: 5,
  loginLockMinutes: 30,
  enablePhoneResetPassword: false,
  enableEmailResetPassword: false
})

const rules: FormRules = {
  maxLoginFailCount: [{ type: 'number', min: 1, message: '次数必须大于0', trigger: 'blur' }],
  loginLockMinutes: [{ type: 'number', min: 1, message: '锁定时间必须大于0', trigger: 'blur' }]
}

const formRef = ref<FormInstance>()

// 获取登录配置
const fetchLoginConfig = async () => {
  loading.value = true
  try {
    const res = await getLoginConfig()
    if (res.success) {
      formData.value = res.data
    }
  } finally {
    loading.value = false
  }
}

// 更新登录配置
const handleUpdateConfig = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    loading.value = true
    const res = await updateLoginConfig(formData.value)
    if (res.success) {
      ElMessage.success('保存成功')
    }
  } catch (error) {
    ElMessage.error(error || '保存失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchLoginConfig()
})
</script>

<template>
  <el-card v-loading="loading" class="w-full">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="160px" class="mt-4">
      <el-form-item label="开启账密登录">
        <el-switch v-model="formData.enablePasswordLogin" />
      </el-form-item>

      <el-form-item label="验证码登录">
        <el-checkbox v-model="formData.enableEmailCodeLogin">邮箱登录</el-checkbox>
        <el-checkbox v-model="formData.enablePhoneCodeLogin">短信登录</el-checkbox>
      </el-form-item>

      <el-form-item label="找回密码方式">
        <el-checkbox v-model="formData.enableEmailResetPassword">邮箱找回</el-checkbox>
        <el-checkbox v-model="formData.enablePhoneResetPassword">短信找回</el-checkbox>
      </el-form-item>

      <el-form-item label="开启登录验证码">
        <el-switch v-model="formData.enableLoginCaptcha" />
      </el-form-item>

      <el-form-item label="最大失败次数" prop="maxLoginFailCount">
        <el-input-number v-model="formData.maxLoginFailCount" :min="1" :max="10" class="w-32" />
        <span class="text-gray-500 ml-2">次后锁定账号</span>
      </el-form-item>

      <el-form-item label="账号锁定时间(分钟)" prop="loginLockMinutes">
        <el-input-number v-model="formData.loginLockMinutes" :min="1" :max="1440" class="w-32" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleUpdateConfig">保存设置</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>
