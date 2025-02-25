<template>
  <el-card>
    <el-form :model="account" label-width="auto" ref="formRef">
      <el-form-item prop="email" label="邮箱">
        <div class="flex items-center justify-between w-full">
          <div class="flex items-center flex-1">
            <el-text class="mr-2">{{ account.email || '未绑定' }}</el-text>
          </div>
          <el-button type="primary" size="small" @click="changeEmail" :disabled="!account.email">
            换绑
          </el-button>
        </div>
      </el-form-item>
      <el-divider style="margin: 0px; margin-top: 8px" />
      <el-form-item prop="password" label="密码" class="mt-6">
        <div class="flex items-center justify-between w-full">
          <el-text>{{ account.password }}</el-text>
          <el-button type="primary" size="small" @click="changePassword">重置</el-button>
        </div>
      </el-form-item>
      <el-divider style="margin: 0px; margin-top: 8px" />
    </el-form>

    <!-- 邮箱换绑弹窗 -->
    <ChangeEmailDialog
      ref="changeEmailDialogRef"
      :current-email="account.email"
      @success="handleEmailChangeSuccess"
    />
  </el-card>
</template>
<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { getCurrentUserAccount } from '~/apis/uc/user'
import ChangeEmailDialog from '~/components/uc/change-email-dialog.vue'

definePageMeta({
  layout: 'user-setting'
})

useSeoMeta({
  title: '账号设置'
})

// Refs
const changeEmailDialogRef = ref()

// Reactive data
const account = reactive({
  email: '',
  phone: '',
  password: '***************'
})

// 加载用户账号信息
const loadAccountInfo = async () => {
  try {
    const res = await getCurrentUserAccount()
    account.email = res.data.email
    account.password = res.data.password
  } catch (error: any) {
    ElMessage.error('获取账号信息失败：' + (error.message || '未知错误'))
  }
}

// 初始化加载
loadAccountInfo()

// Methods
const changeEmail = () => {
  if (!account.email) {
    ElMessage.warning('请先绑定邮箱')
    return
  }
  changeEmailDialogRef.value?.showDialog()
}

const changePassword = () => {
  ElMessage.warning('暂不支持')
}

const handleEmailChangeSuccess = () => {
  // 重新加载账号信息以更新显示的邮箱
  loadAccountInfo()
}
</script>
