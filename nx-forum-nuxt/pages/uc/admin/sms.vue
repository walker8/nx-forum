<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { getSmsConfig, updateSmsConfig, testSms, type SmsConfigDTO } from '@/apis/uc/register'
import { ElMessage } from 'element-plus'

definePageMeta({
  layout: 'uc'
})

// 短信配置
const smsSettings = reactive<SmsConfigDTO>({
  provider: 'ALIYUN',
  accessKeyId: '',
  accessKeySecret: '',
  signName: '',
  verifyCodeTemplateId: '',
  blacklist: [],
  enabled: true
})

// 表单校验规则
const smsRules = reactive<FormRules>({
  provider: [{ required: true, message: '请选择短信服务商', trigger: 'change' }],
  accessKeyId: [{ required: true, message: '请输入访问密钥ID', trigger: 'blur' }],
  accessKeySecret: [{ required: true, message: '请输入访问密钥密码', trigger: 'blur' }],
  signName: [{ required: true, message: '请输入短信签名', trigger: 'blur' }],
  verifyCodeTemplateId: [{ required: true, message: '请输入验证码模板ID', trigger: 'blur' }]
})

const smsFormRef = ref<FormInstance>()

// 测试短信表单
const testPhoneNumber = ref('')
const testPhoneRules = {
  testPhoneNumber: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}
const testFormRef = ref<FormInstance>()

// 黑名单输入
const blacklistInputVisible = ref(false)
const blacklistInputValue = ref('')
const blacklistInputRef = ref<HTMLInputElement>()

// 获取配置
const getSettings = async () => {
  try {
    const res = await getSmsConfig()
    Object.assign(smsSettings, res.data)
  } catch (error) {
    ElMessage.error(error || '获取设置失败')
  }
}

// 保存配置
const saveSmsSettings = async () => {
  if (!smsFormRef.value) return
  await smsFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      await updateSmsConfig(smsSettings)
      ElMessage.success('保存成功')
    } catch (error) {
      ElMessage.error(error || '保存失败')
    }
  })
}

// 发送测试短信
const sendTestSms = async () => {
  if (!testFormRef.value) return
  await testFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      await testSms(testPhoneNumber.value)
      ElMessage.success('测试短信发送成功')
    } catch (error) {
      ElMessage.error(error || '测试短信发送失败')
    }
  })
}

// 黑名单相关方法
const handleBlacklistClose = (phone: string) => {
  smsSettings.blacklist = smsSettings.blacklist.filter((item: string) => item !== phone)
}

const showBlacklistInput = () => {
  blacklistInputVisible.value = true
  nextTick(() => {
    blacklistInputRef.value?.focus()
  })
}

const handleBlacklistInputConfirm = () => {
  if (blacklistInputValue.value && !smsSettings.blacklist.includes(blacklistInputValue.value)) {
    smsSettings.blacklist.push(blacklistInputValue.value)
  }
  blacklistInputVisible.value = false
  blacklistInputValue.value = ''
}

// 生命周期
onMounted(() => {
  getSettings()
})
</script>

<template>
  <el-card shadow="never">
    <el-form ref="smsFormRef" :model="smsSettings" :rules="smsRules" label-width="120px">
      <el-form-item label="短信服务商" prop="provider">
        <el-select v-model="smsSettings.provider">
          <el-option label="阿里云" value="ALIYUN" />
          <el-option label="腾讯云" value="TENCENT" />
        </el-select>
      </el-form-item>

      <el-form-item label="访问密钥ID" prop="accessKeyId">
        <el-input v-model="smsSettings.accessKeyId" />
      </el-form-item>

      <el-form-item label="访问密钥密码" prop="accessKeySecret">
        <el-input v-model="smsSettings.accessKeySecret" type="password" show-password />
      </el-form-item>

      <el-form-item label="短信签名" prop="signName">
        <el-input v-model="smsSettings.signName" />
      </el-form-item>

      <el-form-item label="验证码模板ID" prop="verifyCodeTemplateId">
        <el-input v-model="smsSettings.verifyCodeTemplateId" />
      </el-form-item>

      <el-form-item label="黑名单">
        <el-tag
          v-for="phone in smsSettings.blacklist"
          :key="phone"
          closable
          :disable-transitions="false"
          @close="handleBlacklistClose(phone)"
        >
          {{ phone }}
        </el-tag>
        <el-input
          v-if="blacklistInputVisible"
          ref="blacklistInputRef"
          v-model="blacklistInputValue"
          class="input-new-tag"
          size="small"
          @keyup.enter="handleBlacklistInputConfirm"
          @blur="handleBlacklistInputConfirm"
        />
        <el-button v-else class="button-new-tag" size="small" @click="showBlacklistInput">
          + 添加黑名单
        </el-button>
      </el-form-item>

      <el-form-item>
        <el-checkbox v-model="smsSettings.enabled">启用短信服务</el-checkbox>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="saveSmsSettings">保存设置</el-button>
      </el-form-item>
    </el-form>

    <el-divider>测试短信</el-divider>

    <el-form
      ref="testFormRef"
      :model="{ testPhoneNumber }"
      :rules="testPhoneRules"
      label-width="120px"
    >
      <el-form-item label="手机号码" prop="testPhoneNumber">
        <el-input v-model="testPhoneNumber" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="sendTestSms">发送测试短信</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<style scoped>
.el-tag + .el-tag {
  margin-left: 10px;
}

.button-new-tag {
  margin-left: 10px;
  padding-top: 0;
  padding-bottom: 0;
}

.input-new-tag {
  width: 150px;
  margin-left: 10px;
  vertical-align: bottom;
}
</style>
