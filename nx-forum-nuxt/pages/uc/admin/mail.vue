<script setup lang="ts">
import { ref, reactive, nextTick } from 'vue'
import type { FormInstance, FormRules, FormItemRule, InputInstance } from 'element-plus'
import {
  getSmtpConfig,
  updateSmtpConfig,
  getDomainConfig,
  updateDomainConfig,
  sendTestMail,
  type MailConfigDTO,
  type EmailListConfigDTO,
  type MailTestCmd,
  getMailTemplate,
  updateMailTemplate,
  testVerifyCodeMail,
  testResetPasswordMail,
  testRegisterSuccessMail,
  type MailTemplateDTO,
  type MailTemplateType
} from '@/apis/uc/register'

definePageMeta({
  layout: 'uc'
})

const activeTab = ref('email')

// SMTP settings
const smtpSettings = reactive<MailConfigDTO>({
  host: '',
  port: 465,
  username: '',
  password: '',
  defaultFrom: '',
  auth: true,
  starttlsEnable: true,
  protocol: 'smtps'
})

const smtpRules = reactive<FormRules>({
  host: [{ required: true, message: '请输入SMTP服务器地址', trigger: 'blur' }],
  port: [{ required: true, message: '请输入SMTP端口', trigger: 'blur' }],
  username: [{ required: true, message: '请输入SMTP用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入SMTP密码', trigger: 'blur' }],
  defaultFrom: [{ required: true, message: '请输入发件人昵称', trigger: 'blur' }]
})

const smtpFormRef = ref<FormInstance>()

// Domain whitelist/blacklist
const domainConfig = reactive<EmailListConfigDTO>({
  allowedDomains: [],
  blockedDomains: []
})

// Domain settings
const allowedInputVisible = ref(false)
const blockedInputVisible = ref(false)
const allowedInputValue = ref('')
const blockedInputValue = ref('')
const allowedInputRef = ref<InputInstance>()
const blockedInputRef = ref<InputInstance>()

// Test email
const testEmailForm = reactive<MailTestCmd>({
  to: '',
  subject: '测试邮件',
  content: '这是一封测试邮件',
  html: false
})

const testEmailRules = reactive<FormRules>({
  to: [
    { required: true, message: '请输入收件人邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  subject: [{ required: true, message: '请输入邮件主题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入邮件内容', trigger: 'blur' }]
})

const testEmailFormRef = ref<FormInstance>()

// Mail templates
const templates = reactive<Record<MailTemplateType, MailTemplateDTO>>({
  VERIFY_CODE: {
    subject: '',
    content: '',
    html: false,
    description: ''
  },
  RESET_PASSWORD: {
    subject: '',
    content: '',
    html: false,
    description: ''
  },
  REGISTER_SUCCESS: {
    subject: '',
    content: '',
    html: false,
    description: ''
  }
})

const currentTemplate = ref<MailTemplateType>('VERIFY_CODE')

const templateFormRef = ref<FormInstance>()

const templateRules = reactive<FormRules>({
  subject: [{ required: true, message: '请输入邮件主题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入邮件内容', trigger: 'blur' }]
})

// Template test forms
const verifyCodeTest = reactive({
  to: '',
  variables: {
    code: '123456',
    userName: '测试用户',
    expireMinutes: '5'
  }
})

const resetPasswordTest = reactive({
  to: '',
  variables: {
    code: '123456',
    userName: '测试用户',
    expireMinutes: '30'
  }
})

const registerSuccessTest = reactive({
  to: '',
  variables: {
    userName: '测试用户',
    domain: 'http://localhost:3097'
  }
})

// 添加收件人邮箱验证规则
const emailRules: FormItemRule = {
  required: true,
  type: 'email',
  message: '请输入正确的邮箱地址',
  trigger: 'blur'
}

// Add form refs
const verifyCodeFormRef = ref<FormInstance>()
const resetPasswordFormRef = ref<FormInstance>()
const registerSuccessFormRef = ref<FormInstance>()

// Methods
const getSettings = async () => {
  try {
    const [smtpRes, domainRes] = await Promise.all([getSmtpConfig(), getDomainConfig()])
    Object.assign(smtpSettings, smtpRes.data)
    Object.assign(domainConfig, domainRes.data)
  } catch (error) {
    ElMessage.error(error || '获取设置失败')
  }
}

const saveSmtpSettings = async () => {
  if (!smtpFormRef.value) return
  await smtpFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      await updateSmtpConfig(smtpSettings)
      ElMessage.success('保存成功')
    } catch (error) {
      ElMessage.error(error || '保存失败')
    }
  })
}

const saveDomainConfig = async () => {
  try {
    await updateDomainConfig(domainConfig)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error(error || '保存失败')
  }
}

const sendTestEmail = async () => {
  if (!testEmailFormRef.value) return
  await testEmailFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      await sendTestMail(testEmailForm)
      ElMessage.success('测试邮件发送成功')
    } catch (error) {
      ElMessage.error(error || '测试邮件发送失败')
    }
  })
}

// Methods for templates
const getTemplates = async () => {
  try {
    const types: MailTemplateType[] = ['VERIFY_CODE', 'RESET_PASSWORD', 'REGISTER_SUCCESS']
    const results = await Promise.all(types.map((type) => getMailTemplate(type)))
    results.forEach((res, index) => {
      Object.assign(templates[types[index]], res.data)
    })
  } catch (error) {
    ElMessage.error(error || '获取邮件模板失败')
  }
}

const saveTemplate = async () => {
  if (!templateFormRef.value) return
  await templateFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      await updateMailTemplate(currentTemplate.value, templates[currentTemplate.value])
      ElMessage.success('保存成功')
    } catch (error) {
      ElMessage.error(error || '保存失败')
    }
  })
}

const testTemplate = async (type: MailTemplateType) => {
  let formRef: FormInstance | undefined
  let testData = {
    to: '',
    variables: {}
  }

  switch (type) {
    case 'VERIFY_CODE':
      formRef = verifyCodeFormRef.value
      testData.to = verifyCodeTest.to
      testData.variables = verifyCodeTest.variables
      break
    case 'RESET_PASSWORD':
      formRef = resetPasswordFormRef.value
      testData.to = resetPasswordTest.to
      testData.variables = resetPasswordTest.variables
      break
    case 'REGISTER_SUCCESS':
      formRef = registerSuccessFormRef.value
      testData.to = registerSuccessTest.to
      testData.variables = registerSuccessTest.variables
      break
  }

  if (!formRef) return

  try {
    // Validate form before sending
    await formRef.validate(async (valid) => {
      if (!valid) return
      try {
        switch (type) {
          case 'VERIFY_CODE':
            await testVerifyCodeMail(testData)
            break
          case 'RESET_PASSWORD':
            await testResetPasswordMail(testData)
            break
          case 'REGISTER_SUCCESS':
            await testRegisterSuccessMail(testData)
            break
        }
        ElMessage.success('测试邮件发送成功')
      } catch (error) {
        ElMessage.error(error || '测试邮件发送失败')
      }
    })
  } catch (error) {
    ElMessage.error(error || '测试邮件发送失败')
  }
}

// Lifecycle
onMounted(() => {
  getSettings()
  getTemplates()
})

const handleAllowedClose = (domain: string) => {
  domainConfig.allowedDomains = domainConfig.allowedDomains.filter((d) => d !== domain)
}

const handleBlockedClose = (domain: string) => {
  domainConfig.blockedDomains = domainConfig.blockedDomains.filter((d) => d !== domain)
}

const showAllowedInput = () => {
  allowedInputVisible.value = true
  nextTick(() => {
    allowedInputRef.value?.input?.focus()
  })
}

const showBlockedInput = () => {
  blockedInputVisible.value = true
  nextTick(() => {
    blockedInputRef.value?.input?.focus()
  })
}

const handleAllowedInputConfirm = () => {
  if (allowedInputValue.value && !domainConfig.allowedDomains.includes(allowedInputValue.value)) {
    domainConfig.allowedDomains.push(allowedInputValue.value)
  }
  allowedInputVisible.value = false
  allowedInputValue.value = ''
}

const handleBlockedInputConfirm = () => {
  if (blockedInputValue.value && !domainConfig.blockedDomains.includes(blockedInputValue.value)) {
    domainConfig.blockedDomains.push(blockedInputValue.value)
  }
  blockedInputVisible.value = false
  blockedInputValue.value = ''
}
</script>

<template>
  <el-card shadow="never">
    <el-tabs v-model="activeTab">
      <!-- SMTP Settings Tab -->
      <el-tab-pane label="SMTP设置" name="email">
        <el-form ref="smtpFormRef" :model="smtpSettings" :rules="smtpRules" label-width="120px">
          <el-form-item label="SMTP服务器" prop="host">
            <el-input v-model="smtpSettings.host" />
          </el-form-item>
          <el-form-item label="SMTP端口" prop="port">
            <el-input-number v-model="smtpSettings.port" :min="1" :max="65535" />
          </el-form-item>
          <el-form-item label="SMTP用户名" prop="username">
            <el-input v-model="smtpSettings.username" />
          </el-form-item>
          <el-form-item label="SMTP密码" prop="password">
            <el-input v-model="smtpSettings.password" type="password" show-password />
          </el-form-item>
          <el-form-item label="发件人昵称" prop="defaultFrom">
            <el-input v-model="smtpSettings.defaultFrom" />
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="smtpSettings.auth">启用认证</el-checkbox>
            <el-checkbox v-model="smtpSettings.starttlsEnable" class="ml-4">
              启用STARTTLS
            </el-checkbox>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveSmtpSettings">保存设置</el-button>
          </el-form-item>
        </el-form>

        <el-divider>测试邮件</el-divider>
        <el-form
          ref="testEmailFormRef"
          :model="testEmailForm"
          :rules="testEmailRules"
          label-width="120px"
        >
          <el-form-item label="收件人" prop="to">
            <el-input v-model="testEmailForm.to" />
          </el-form-item>
          <el-form-item label="主题" prop="subject">
            <el-input v-model="testEmailForm.subject" />
          </el-form-item>
          <el-form-item label="内容" prop="content">
            <el-input v-model="testEmailForm.content" type="textarea" :rows="3" />
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="testEmailForm.html">HTML格式</el-checkbox>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="sendTestEmail">发送测试邮件</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- Domain Settings Tab -->
      <el-tab-pane label="域名设置" name="domain">
        <el-alert type="info" show-icon class="mb-6">
          <p>允许的邮箱域名的优先级比禁止的邮箱域名高</p>
          <p>允许的邮箱域名为空则表示所有邮箱域名为白名单</p>
        </el-alert>

        <div class="mt-4">
          <el-text>允许的邮箱域名</el-text>
          <div class="mt-2">
            <el-tag
              v-for="domain in domainConfig.allowedDomains"
              :key="domain"
              closable
              :disable-transitions="false"
              @close="handleAllowedClose(domain)"
            >
              {{ domain }}
            </el-tag>
            <el-input
              v-if="allowedInputVisible"
              ref="allowedInputRef"
              v-model="allowedInputValue"
              class="input-new-tag"
              size="small"
              @keyup.enter="handleAllowedInputConfirm"
              @blur="handleAllowedInputConfirm"
            />
            <el-button v-else class="button-new-tag" size="small" @click="showAllowedInput">
              + 添加域名
            </el-button>
          </div>
        </div>

        <div class="mt-4">
          <el-text>禁止的邮箱域名</el-text>
          <div class="mt-2">
            <el-tag
              v-for="domain in domainConfig.blockedDomains"
              :key="domain"
              closable
              type="danger"
              :disable-transitions="false"
              @close="handleBlockedClose(domain)"
            >
              {{ domain }}
            </el-tag>
            <el-input
              v-if="blockedInputVisible"
              ref="blockedInputRef"
              v-model="blockedInputValue"
              class="input-new-tag"
              size="small"
              @keyup.enter="handleBlockedInputConfirm"
              @blur="handleBlockedInputConfirm"
            />
            <el-button v-else class="button-new-tag" size="small" @click="showBlockedInput">
              + 添加域名
            </el-button>
          </div>
        </div>

        <el-button type="primary" class="mt-4" @click="saveDomainConfig">保存设置</el-button>
      </el-tab-pane>

      <!-- Mail Templates Tab -->
      <el-tab-pane label="邮件模板" name="template">
        <div class="w-200 mr-4">
          <el-radio-group v-model="currentTemplate">
            <el-radio-button value="VERIFY_CODE">验证码邮件</el-radio-button>
            <el-radio-button value="RESET_PASSWORD">重置密码邮件</el-radio-button>
            <el-radio-button value="REGISTER_SUCCESS">注册成功邮件</el-radio-button>
          </el-radio-group>
        </div>

        <div class="mt-5">
          <el-form
            ref="templateFormRef"
            :model="templates[currentTemplate]"
            :rules="templateRules"
            label-width="120px"
          >
            <el-form-item label="模板说明" prop="description">
              <el-input v-model="templates[currentTemplate].description" />
            </el-form-item>
            <el-form-item label="邮件主题" prop="subject">
              <el-input v-model="templates[currentTemplate].subject" />
            </el-form-item>
            <el-form-item label="邮件内容" prop="content">
              <el-input v-model="templates[currentTemplate].content" type="textarea" :rows="8" />
            </el-form-item>
            <el-form-item>
              <el-checkbox v-model="templates[currentTemplate].html">HTML格式</el-checkbox>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveTemplate">保存模板</el-button>
            </el-form-item>
          </el-form>

          <el-divider>测试发送</el-divider>

          <!-- Verify Code Test -->
          <template v-if="currentTemplate === 'VERIFY_CODE'">
            <el-form ref="verifyCodeFormRef" :model="verifyCodeTest" label-width="120px">
              <el-form-item label="收件人" prop="to" :rules="[emailRules]">
                <el-input v-model="verifyCodeTest.to" />
              </el-form-item>
              <el-form-item label="验证码">
                <el-input v-model="verifyCodeTest.variables.code" />
              </el-form-item>
              <el-form-item label="用户名">
                <el-input v-model="verifyCodeTest.variables.userName" />
              </el-form-item>
              <el-form-item label="过期时间(分钟)">
                <el-input v-model="verifyCodeTest.variables.expireMinutes" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="testTemplate('VERIFY_CODE')">
                  发送测试邮件
                </el-button>
              </el-form-item>
            </el-form>
          </template>

          <!-- Reset Password Test -->
          <template v-if="currentTemplate === 'RESET_PASSWORD'">
            <el-form ref="resetPasswordFormRef" :model="resetPasswordTest" label-width="120px">
              <el-form-item label="收件人" prop="to" :rules="[emailRules]">
                <el-input v-model="resetPasswordTest.to" />
              </el-form-item>
              <el-form-item label="验证码">
                <el-input v-model="resetPasswordTest.variables.code" />
              </el-form-item>
              <el-form-item label="用户名">
                <el-input v-model="resetPasswordTest.variables.userName" />
              </el-form-item>
              <el-form-item label="过期时间(分钟)">
                <el-input v-model="resetPasswordTest.variables.expireMinutes" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="testTemplate('RESET_PASSWORD')">
                  发送测试邮件
                </el-button>
              </el-form-item>
            </el-form>
          </template>

          <!-- Register Success Test -->
          <template v-if="currentTemplate === 'REGISTER_SUCCESS'">
            <el-form ref="registerSuccessFormRef" :model="registerSuccessTest" label-width="120px">
              <el-form-item label="收件人" prop="to" :rules="[emailRules]">
                <el-input v-model="registerSuccessTest.to" />
              </el-form-item>
              <el-form-item label="用户名">
                <el-input v-model="registerSuccessTest.variables.userName" />
              </el-form-item>
              <el-form-item label="网站域名">
                <el-input v-model="registerSuccessTest.variables.domain" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="testTemplate('REGISTER_SUCCESS')">
                  发送测试邮件
                </el-button>
              </el-form-item>
            </el-form>
          </template>
        </div>
      </el-tab-pane>
    </el-tabs>
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
  width: 90px;
  margin-left: 10px;
  vertical-align: bottom;
}
</style>
