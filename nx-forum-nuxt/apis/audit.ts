import type { UserVO } from '~/types/global'
import request from '../utils/request'

// 敏感词配置
export interface AuditConfigSensitiveWordsDTO {
  enableSensitiveWordsAudit: boolean
  sensitiveWords: string[]
}

export interface AuditConfigBlackWhiteUsersVO {
  whiteListUsers: UserVO[]
  blackListUsers: UserVO[]
}

// 审核规则命中动作
export type AuditRuleAction = 'PASS' | 'REVIEW' | 'REJECT'

// QLExpress 表达式规则
export interface AuditRule {
  id: string
  name: string
  expression: string
  action: AuditRuleAction
  enabled: boolean
  priority: number
}

export interface AuditConfigRulesDTO {
  enabled: boolean
  rules: AuditRule[]
}

// AI 审核场景配置（模型引用共享模型库）
export interface AuditConfigAiDTO {
  enabled: boolean
  activeModelId: string
  historyCount: number
  prompt: string
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

// 获取审核规则配置
export function getAuditConfigRules() {
  return request.get('/v1/admin/audits/rules')
}

// 更新审核规则配置
export function updateAuditConfigRules(data: AuditConfigRulesDTO) {
  return request.put('/v1/admin/audits/rules', data)
}

// 校验规则表达式（返回错误信息，空串表示合法）
export function testRule(data: { expression: string }) {
  return request.post('/v1/admin/audits/rules/test', data)
}

// 获取 AI 审核配置
export function getAuditConfigAi() {
  return request.get('/v1/admin/audits/ai-config')
}

// 更新 AI 审核配置
export function updateAuditConfigAi(data: AuditConfigAiDTO) {
  return request.put('/v1/admin/audits/ai-config', data)
}

// 测试 AI 审核（用选中模型 + 审核提示词跑一次判定）
export function testAiAudit(data: { modelId: string; prompt?: string; content?: string }) {
  return request.post('/v1/admin/audits/ai-config/test', data)
}
