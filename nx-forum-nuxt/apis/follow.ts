import type { Query } from '~/types/global'
import Http from '../utils/request'

export interface UserFollowCmd {
  followUserId: number
  remark?: string
}

export interface UserFollowVO {
  userId: number
  userName: string
  avatar: string
  intro: string
  remark: string
  followTime: string
  followed: boolean
}

/**
 * 关注用户
 * @param data 关注用户请求参数
 * @returns
 */
export const followUser = (data: UserFollowCmd) => {
  return Http.post('/v1/users/follow', data)
}

/**
 * 取消关注
 * @param followUserId 被关注用户ID
 * @returns
 */
export const unfollowUser = (followUserId: number) => {
  return Http.post(`/v1/users/unfollow/${followUserId}`)
}

/**
 * 获取用户关注列表
 * @param userId 用户ID
 * @param query 分页查询参数
 * @returns
 */
export const getFollowingList = (userId: number, query: Query) => {
  return Http.get(`/v1/users/${userId}/following`, query)
}

/**
 * 获取用户粉丝列表
 * @param userId 用户ID
 * @param query 分页查询参数
 * @returns
 */
export const getFollowerList = (userId: number, query: Query) => {
  return Http.get(`/v1/users/${userId}/followers`, query)
}
