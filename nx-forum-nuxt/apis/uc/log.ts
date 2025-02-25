import type { ApiResponse } from '~/types/global'
import http from '~/utils/request'

/**
 * 用户日志查询参数
 */
export interface UserLogQueryParams {
  userId?: number
  ipAddress?: string
  logType?: number
  pageNo?: number
  pageSize?: number
  orderBy?: string
}

/**
 * 用户日志对象
 */
export interface UserLogVO {
  logId: number
  logType: number
  logTypeDesc: string
  logContent: string
  ipAddress: string
  userAgent: string
  operationStatus: number
  operationStatusDesc: string
  userId: number
  userName: string
  createTime: string
}

/**
 * 用户日志分页结果
 */
export interface UserLogPageResult {
  records: UserLogVO[]
  total: number
  size: number
  current: number
  hasNext: boolean
}

/**
 * 查询用户日志列表
 * @param params 查询参数
 * @returns Promise
 */
export const queryUserLogs = (
  params: UserLogQueryParams
): Promise<ApiResponse<UserLogPageResult>> => {
  return http.get('/v1/uc/admin/user-logs', params)
}
