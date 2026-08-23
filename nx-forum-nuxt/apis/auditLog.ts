import request from '../utils/request'

/**
 * 审核阶段枚举
 * 1 黑白名单 2 敏感词 3 规则引擎 4 AI 5 人工
 */
export type AuditStageV = 'BLACK_WHITE' | 'SENSITIVE_WORD' | 'RULE_ENGINE' | 'AI' | 'MANUAL'

/**
 * 审核触发来源
 * 1 主动 2 编辑 3 举报 4 管理员后台
 */
export type AuditTriggerSourceV = 'NEW' | 'EDIT' | 'REPORT' | 'MANUAL'

/**
 * 审核状态
 * 0 通过 1 审核中 2 拒绝 3 忽略
 */
export type AuditStatusV = 'PASSED' | 'AUDITING' | 'REJECTED' | 'IGNORED'

/**
 * 操作人类型
 * 1 系统 2 管理员
 */
export type OperatorTypeV = 'SYSTEM' | 'ADMIN'

/**
 * 审核记录 VO
 */
export interface AuditLogVO {
  logId: number
  contentType: number
  contentTypeName: string
  contentId: number | null
  forumId: number | null
  forumName: string
  userId: number
  authorName: string
  userIp?: string
  ipLocation?: string
  auditSessionId?: string
  triggerSource: AuditTriggerSourceV | number
  triggerSourceName: string
  auditStage: AuditStageV | number
  auditStageName: string
  auditStatusBefore?: AuditStatusV | number | null
  auditStatusAfter: AuditStatusV | number
  auditStatusAfterName: string
  hitDetail?: string
  hitDetailObj?: Record<string, any>
  operatorId?: number | null
  operatorName?: string
  operatorType: OperatorTypeV | number
  operatorTypeName: string
  reason?: string
  costMs?: number
  contentSnapshot?: string
  createTime: string
}

/**
 * 审核记录分页查询参数
 */
export interface AuditLogPageQuery {
  pageNo?: number
  pageSize?: number
  contentType?: number
  contentId?: number
  userId?: number
  forumId?: number
  auditStage?: number
  triggerSource?: number
  operatorType?: number
  auditStatusAfter?: number
}

/**
 * 分页查询审核记录
 */
export function pageAuditLogs(params: AuditLogPageQuery) {
  return request.get('/v1/admin/audit-logs', params)
}

/**
 * 查询单条审核记录详情
 */
export function getAuditLog(logId: number) {
  return request.get(`/v1/admin/audit-logs/${logId}`)
}

/**
 * 查询某内容的所有审核记录
 */
export function listAuditLogsByContent(contentType: number, contentId: number) {
  return request.get('/v1/admin/audit-logs/by-content', { contentType, contentId })
}

/**
 * 审核阶段字典
 */
export const AUDIT_STAGE_MAP: Record<string, { label: string; type: string }> = {
  '1': { label: '黑白名单', type: 'danger' },
  '2': { label: '敏感词', type: 'warning' },
  '3': { label: '规则引擎', type: 'warning' },
  '4': { label: 'AI 审核', type: 'primary' },
  '5': { label: '人工复审', type: 'success' }
}

/**
 * 审核状态字典
 */
export const AUDIT_STATUS_MAP: Record<string, { label: string; type: string }> = {
  '0': { label: '通过', type: 'success' },
  '1': { label: '审核中', type: 'warning' },
  '2': { label: '拒绝', type: 'danger' },
  '3': { label: '忽略', type: 'info' }
}

/**
 * 内容类型字典
 */
export const CONTENT_TYPE_MAP: Record<number, { label: string }> = {
  1: { label: '主贴' },
  2: { label: '评论' },
  3: { label: '楼中楼' }
}

/**
 * 触发来源字典
 */
export const TRIGGER_SOURCE_MAP: Record<number | string, string> = {
  1: '主动发布',
  2: '编辑触发',
  3: '举报触发',
  4: '管理员后台'
}

/**
 * 操作人类型字典
 */
export const OPERATOR_TYPE_MAP: Record<number | string, string> = {
  1: '系统',
  2: '管理员'
}

/**
 * 根据内容类型和内容ID拼接跳转 URL
 */
export function buildContentLink(
  contentType: number | null | undefined,
  contentId: number | null | undefined
): string | null {
  if (!contentType || !contentId) return null
  if (contentType === 1) return `/t/${contentId}`
  // 评论/楼中楼 跳转所属主题帖
  return null // 评论/楼中楼需要先查 threadId，由详情页跳转
}
