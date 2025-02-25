import { type RoleCreateCmd, type RoleUpdateCmd } from '~/types/global'
import Http from '../../utils/request'

// 查询角色列表
export const queryRoles = (params: any) => {
  return Http.get('/v1/uc/admin/roles', params)
}

// 获取角色详情
export const getRoleInfo = (roleId: number) => {
  return Http.get(`/v1/uc/admin/roles/${roleId}`)
}

// 新增角色
export const createRole = (role: RoleCreateCmd) => {
  return Http.post('/v1/uc/admin/roles', role)
}

// 修改角色
export const updateRole = (role: RoleUpdateCmd) => {
  return Http.put('/v1/uc/admin/roles', role)
}

// 删除角色
export const deleteRole = (roleId: number) => {
  return Http.delete(`/v1/uc/admin/roles/${roleId}`)
}

// 获取用户角色列表
export const getUserRoleList = (userId: number) => {
  return Http.get(`/v1/uc/admin/user-roles/user/${userId}`)
}

// 分配用户角色
export const assignUserRole = (userRole: any) => {
  return Http.post('/v1/uc/admin/user-roles', userRole)
}

// 取消用户角色
export const cancelUserRole = (id: number) => {
  return Http.delete(`/v1/uc/admin/user-roles/${id}`)
}

/**
 * 分页查询角色授权的用户列表
 */
export function queryUserRoles(params: {
  userId?: number
  pageNo?: number
  pageSize?: number
  roleKey?: string
  userName?: string
}) {
  return Http.get('/v1/uc/admin/user-roles/', params)
}
