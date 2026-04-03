import type { CreateReportCommand, HandleReportCommand } from '~/types/global'
import Http from '../utils/request'
/**
 * 创建举报
 * @param params
 * @returns
 */
export function createReport(params: CreateReportCommand) {
  return Http.post('/v1/report/create', params)
}

/**
 * 查询举报列表
 * @param params
 * @returns
 */
export function queryReports(params: any) {
  return Http.get('/v1/admin/report/list', params)
}

/**
 * 处理举报
 * @param params
 * @returns
 */
export function handleReport(params: HandleReportCommand & { notice?: boolean }) {
  return Http.post('/v1/admin/report/handle', params)
}
