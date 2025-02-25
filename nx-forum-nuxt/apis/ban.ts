import Http from '../utils/request'
import type { BanCreateCmd } from '~/types/global'

/**
 * 分页查询禁言记录
 * @param params 查询参数
 */
export const queryBans = (params: {
  pageNo?: number
  pageSize?: number
  userId?: number
  forumId?: number
}) => {
  return Http.get('/v1/admin/bans', params)
}

/**
 * 禁言用户
 * @param cmd 禁言参数
 */
export const createBan = (cmd: BanCreateCmd) => {
  return Http.post('/v1/admin/bans', cmd)
}

/**
 * 解除禁言
 * @param cmd 解除禁言参数
 */
export const unbanUser = (cmd: BanCreateCmd) => {
  return Http.post('/v1/admin/bans/unban', cmd)
} 