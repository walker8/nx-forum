import Http from '../utils/request'

/**
 * 查询用户权限,只在客户端调用
 * @param forumId
 * @returns
 */
export const queryPermissions = (forumId: number = 0) => {
  const url = forumId && forumId > 0
    ? `/v1/auth/permissions?forumId=${forumId}`
    : '/v1/auth/permissions'
  return Http.get(url)
}
