import Http from '../../utils/request'

export interface MailConfigDTO {
  host: string
  port: number
  username: string
  password: string
  defaultFrom: string
  auth: boolean
  starttlsEnable: boolean
  protocol: string
}

export interface EmailListConfigDTO {
  allowedDomains: string[]
  blockedDomains: string[]
}

export interface MailTestCmd {
  to: string
  from?: string
  subject: string
  content: string
  html?: boolean
}

export type MailTemplateType = 'VERIFY_CODE' | 'RESET_PASSWORD' | 'REGISTER_SUCCESS'

export interface MailTemplateDTO {
  subject: string
  content: string
  html?: boolean
  description?: string
}

export interface TemplateTestCmd {
  to: string
  variables?: Record<string, string>
}

export interface RegisterConfigDTO {
  enableRegister: boolean
  enableSmsRegister: boolean
  enableEmailRegister: boolean
  enableRegisterCaptcha: boolean
  forbiddenUsernames: string[]
  passwordMinLength: number
  passwordMaxLength: number
  passwordStrength: number
}

// 短信配置相关类型定义
export interface SmsConfigDTO {
  provider: 'ALIYUN' | 'TENCENT'
  accessKeyId: string
  accessKeySecret: string
  signName: string
  verifyCodeTemplateId: string
  blacklist: string[]
  enabled: boolean
}

/**
 * 获取SMTP配置
 */
export function getSmtpConfig() {
  return Http.get('/v1/uc/admin/mail/smtp/config')
}

/**
 * 更新SMTP配置
 */
export function updateSmtpConfig(data: MailConfigDTO) {
  return Http.post('/v1/uc/admin/mail/smtp/config', data)
}

/**
 * 获取邮件域名黑白名单配置
 */
export function getDomainConfig() {
  return Http.get('/v1/uc/admin/mail/domain/config')
}

/**
 * 更新邮件域名黑白名单配置
 */
export function updateDomainConfig(data: EmailListConfigDTO) {
  return Http.post('/v1/uc/admin/mail/domain/config', data)
}

/**
 * 发送测试邮件
 */
export function sendTestMail(data: MailTestCmd) {
  return Http.post('/v1/uc/admin/mail/test', data)
}

/**
 * 获取邮件模板
 */
export function getMailTemplate(type: MailTemplateType) {
  return Http.get(`/v1/uc/admin/mail/template/${type}`)
}

/**
 * 更新邮件模板
 */
export function updateMailTemplate(type: MailTemplateType, data: MailTemplateDTO) {
  return Http.post(`/v1/uc/admin/mail/template/${type}`, data)
}

/**
 * 测试验证码邮件
 */
export function testVerifyCodeMail(data: TemplateTestCmd) {
  return Http.post('/v1/uc/admin/mail/template/test/verify-code', data)
}

/**
 * 测试重置密码邮件
 */
export function testResetPasswordMail(data: TemplateTestCmd) {
  return Http.post('/v1/uc/admin/mail/template/test/reset-password', data)
}

/**
 * 测试注册成功邮件
 */
export function testRegisterSuccessMail(data: TemplateTestCmd) {
  return Http.post('/v1/uc/admin/mail/template/test/register-success', data)
}

export const getRegisterConfig = () => {
  return Http.get('/v1/uc/admin/register/config')
}

export const updateRegisterConfig = (data: RegisterConfigDTO) => {
  return Http.post('/v1/uc/admin/register/config', data)
}

export const checkUsername = (username: string) => {
  return Http.get('/v1/uc/users/username/check', { username })
}

// 获取短信配置
export const getSmsConfig = () => {
  return Http.get('/v1/uc/admin/sms/config')
}

// 更新短信配置
export const updateSmsConfig = (data: SmsConfigDTO) => {
  return Http.post('/v1/uc/admin/sms/config', data)
}

// 发送测试短信
export const testSms = (phone: string) => {
  return Http.get('/v1/uc/admin/sms/test', { phoneNumber: phone })
}
