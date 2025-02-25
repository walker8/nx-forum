import { type PermissionCreateCmd, type PermissionUpdateCmd } from '~/types/global'
import Http from '../../utils/request'

// 获取权限列表
export const getPermissionList = () => {
  return Http.get('/v1/uc/admin/permissions')
}

// 获取权限详情
export const getPermissionInfo = (permId: number) => {
  return Http.get(`/v1/uc/admin/permissions/${permId}`)
}

// 新增权限
export const createPermission = (permission: PermissionCreateCmd) => {
  return Http.post('/v1/uc/admin/permissions', permission)
}

// 修改权限
export const updatePermission = (permission: PermissionUpdateCmd) => {
  return Http.put('/v1/uc/admin/permissions', permission)
}

// 删除权限
export const deletePermission = (permId: number) => {
  return Http.delete(`/v1/uc/admin/permissions/${permId}`)
}

// 启用权限
export const enablePermission = (permId: number) => {
  return Http.put(`/v1/uc/admin/permissions/${permId}/enable`)
}

// 禁用权限
export const disablePermission = (permId: number) => {
  return Http.put(`/v1/uc/admin/permissions/${permId}/disable`)
}

// 获取角色的权限列表
export const getRolePermissionList = (roleKey: string) => {
  return Http.get(`/v1/uc/admin/permissions/role/${roleKey}`)
}

// 分配角色权限
export const assignRolePermission = (rolePermission: any) => {
  return Http.post('/v1/uc/admin/role-permissions', rolePermission)
}

// 取消角色权限
export const cancelRolePermission = (id: number) => {
  return Http.delete(`/v1/uc/admin/role-permissions/${id}`)
}

// 获取角色权限列表
export const getRolePermissions = (roleKey: string) => {
  return Http.get(`/v1/uc/admin/role-permissions/role/${roleKey}`)
}
