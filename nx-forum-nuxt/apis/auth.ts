/**
 * 查询用户权限,只在客户端调用
 * @param forumId
 * @returns
 */
export const queryPermissions = (forumId: number = 0) => {
  const baseUrl = '/nx-forum'
  const url = forumId && forumId > 0
    ? `${baseUrl}/v1/auth/permissions?forumId=${forumId}`
    : `${baseUrl}/v1/auth/permissions`
  return $fetch(url)
}
