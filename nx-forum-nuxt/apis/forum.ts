import Http from '../utils/request'

export type ForumCmd = {
  // 论坛ID
  forumId?: number | null
  // 论坛号，只能用英文或数字
  name: string
  // 论坛名称
  nickName: string
  // 论坛20字短介绍 纯文本
  shortBrief?: string
  // 论坛简介 允许HTML
  brief?: string
  // 访问控制 0 开放 1 关闭
  forumAccess?: number
  // 论坛图标
  iconName: string
  // SEO 标题，如果设置会代替版块名称
  seoTitle?: string | null
  // SEO关键字，不要超过6个，关键字之间用英文的,隔开
  seoKeywords?: string | null
  // SEO content
  seoContent?: string | null
  // SEO帖子后缀，保留
  seoExt?: string | null
  // 论坛背景图片
  backgroundImage?: string | null
  // 菜单栏是否显示
  showMenu?: boolean | null
  // 菜单排序
  menuOrder?: number | null
}

export type ForumItemVO = {
  forumId: number
  name: string
  nickName: string
  iconName: string
  showMenu: boolean
  isSystem: boolean
  menuOrder: number
  createTime: string
  defaultForum: boolean
}

export const getForumShowMenu = () => {
  return Http.get(`/v1/forums/menu`)
}

/**
 * 获取用户版块列表
 * @returns
 */
export const getUserForumList = () => {
  return Http.get(`/v1/forums/user`)
}

export const getUserForumMenu = () => {
  return Http.get(`/v1/forums/menu/user`)
}

export const getAdminForumMenu = () => {
  return Http.get(`/v1/admin/forums/menu`)
}

export const getForumInfoByName = (forumName: string) => {
  return Http.get(`/v1/forums/info?forumName=${forumName}`)
}

export const createForum = (forumCmd: ForumCmd) => {
  return Http.post(`/v1/admin/forums`, forumCmd)
}

export const updateForum = (data: any) => {
  return Http.put('/v1/admin/forums', data)
}

export const getForumById = (forumId: number) => {
  return Http.get(`/v1/admin/forums/${forumId}`)
}

export const deleteForum = (forumId: number) => {
  return Http.delete(`/v1/admin/forums/${forumId}`)
}

export const getAllForumByAdmin = () => {
  return Http.get(`/v1/admin/forums`)
}

export const updateDefaultForumByAdmin = (forumId: number) => {
  return Http.put(`/v1/admin/forums/default?forumId=${forumId}`)
}

export const getForumAccess = (forumId: number) => {
  return Http.get(`/v1/admin/forum-access/${forumId}`)
}

export const batchUpdateForumAccess = (
  forumId: number,
  permissions: any,
  enableForumAccess: boolean = true
) => {
  return Http.put(
    `/v1/admin/forum-access/batch?forumId=${forumId}&enableForumAccess=${enableForumAccess}`,
    permissions
  )
}

export type UserRoleVO = {
  id: number
  userName: string
  roleKey: string
  roleScope: string
  createUserName: string
  createTime: string
  expireTime: string
}

// 查询版块用户角色
export const queryForumUserRoles = (params: {
  forumId: number
  pageNo?: number
  pageSize?: number
  roleKey?: string
  userName?: string
}) => {
  return Http.get(`/v1/admin/auth/user-roles`, params)
}

// 授权版块用户角色
export const assignForumUserRole = (params: {
  forumId: number
  userId: number
  roleKey: string
  expireTime?: string
}) => {
  return Http.post(`/v1/admin/auth/user-roles/assign`, params)
}

// 取消版块用户角色
export const cancelForumUserRole = (forumId: number, roleKey: string) => {
  return Http.post(`/v1/admin/auth/user-roles/cancel?forumId=${forumId}&roleKey=${roleKey}`)
}

export const getForumUserInfo = (userId: number) => {
  return Http.get(`/v1/users/${userId}/info`)
}
