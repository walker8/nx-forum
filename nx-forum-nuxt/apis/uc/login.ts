import Http from '~/utils/request'
export interface LoginConfigDTO {
  enablePasswordLogin: boolean
  enableEmailCodeLogin: boolean
  enablePhoneCodeLogin: boolean
  enablePhoneResetPassword: boolean
  enableEmailResetPassword: boolean
  enableLoginCaptcha: boolean
  maxLoginFailCount: number
  loginLockMinutes: number
}

export interface AuthConfigVO {
  loginConfig?: LoginConfigVO
  registerConfig?: RegisterConfigVO
}

export interface LoginConfigVO {
  enablePhoneCodeLogin: boolean
  enableEmailCodeLogin: boolean
  enablePasswordLogin: boolean
  enablePhoneResetPassword: boolean
  enableEmailResetPassword: boolean
}

export interface RegisterConfigVO {
  enableRegister: boolean
  enableSmsRegister: boolean
  enableEmailRegister: boolean
  passwordMinLength: number
  passwordMaxLength: number
  passwordStrength: number
}

export const login = (user: any) => {
  return Http.post(`/v1/uc/auth/login`, user)
}

export const logout = () => {
  return Http.post(`/v1/uc/auth/logout`)
}

export const getLoginConfig = () => {
  return Http.get('/v1/uc/admin/login/config')
}

export const updateLoginConfig = (data: LoginConfigDTO) => {
  return Http.post('/v1/uc/admin/login/config', data)
}

export interface VerifyCodeLoginDTO {
  account: string // 账号(手机号/邮箱)
  code: string // 验证码
  type: string // 账号类型(phone/email)
}

// 验证码登录
export const verifyCodeLogin = (data: VerifyCodeLoginDTO) => {
  return Http.post('/v1/uc/verify-code/login', data)
}

// 发送手机验证码
export const sendPhoneVerifyCode = (phone: string, type: string = 'login') => {
  return Http.post(`/v1/uc/verify-code/phone?phone=${phone}&type=${type}`)
}

// 发送邮箱验证码
export const sendEmailVerifyCode = (email: string, type: string = 'login') => {
  return Http.post(`/v1/uc/verify-code/email?email=${email}&type=${type}`)
}

// 添加重置密码接口
export const resetPasswordByVerifyCode = (data: any) => {
  return Http.post('/v1/uc/verify-code/reset-password', data)
}

export const getAuthConfig = () => {
  return Http.get('/v1/uc/auth/config')
}
