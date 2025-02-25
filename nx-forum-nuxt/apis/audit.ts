import type { UserVO } from '~/types/global'
import request from '../utils/request'

// 根据openapi.json最新定义调整类型
export interface AuditConfigSensitiveWordsDTO {
  enableSensitiveWordsAudit: boolean
  sensitiveWords: string[]
}

export interface AuditConfigBlackWhiteUsersVO {
  whiteListUsers: UserVO[]
  blackListUsers: UserVO[]
}

// 获取敏感词配置
export function getAuditConfigSensitiveWords() {
  return request.get('/v1/admin/audits/sensitive-words')
}

// 更新敏感词配置
export function updateAuditConfigSensitiveWords(data: AuditConfigSensitiveWordsDTO) {
  return request.put('/v1/admin/audits/sensitive-words', data)
}

// 获取黑白名单配置（使用VO接口）
export function getAuditConfigBlackWhiteUsersVO() {
  return request.get('/v1/admin/audits/black-white-users')
}

// 更新黑白名单配置
export function updateAuditConfigBlackWhiteUsers(data: {
  whiteListUsers: number[]
  blackListUsers: number[]
}) {
  return request.put('/v1/admin/audits/black-white-users', data)
}
