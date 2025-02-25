package com.leyuz.uc.auth.role.mybatis;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leyuz.uc.auth.role.RolePermissionPO;

import java.util.List;

public interface IRolePermissionService extends IService<RolePermissionPO> {
    List<RolePermissionPO> listByRoleKey(String roleKey);

    /**
     * 删除角色的所有权限
     *
     * @param roleKey 角色标识
     * @return 是否删除成功
     */
    boolean removeByRoleKey(String roleKey);
}