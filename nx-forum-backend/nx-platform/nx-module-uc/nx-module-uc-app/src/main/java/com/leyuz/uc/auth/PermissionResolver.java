package com.leyuz.uc.auth;

import com.leyuz.common.utils.HeaderUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PermissionResolver {
    private final AuthorizationApplication authorizationApplication;

    public boolean hasPermission(String permission, String roleScope, String defaultRoleKey) {
        return authorizationApplication.hasPermission(HeaderUtils.getUserId(), permission, roleScope, defaultRoleKey);
    }

    public boolean hasPermission(String permission, String defaultRoleKey) {
        return hasPermission(permission, null, defaultRoleKey);
    }

    public boolean hasPermission(String permission) {
        return hasPermission(permission, null, null);
    }

    public boolean hasRole(String roleKey, String roleScope, String defaultRoleKey) {
        return authorizationApplication.hasRole(HeaderUtils.getUserId(), roleKey, roleScope, defaultRoleKey);
    }

    public boolean hasRole(String roleKey, String defaultRoleKey) {
        return hasRole(roleKey, null, defaultRoleKey);
    }

    public boolean hasRole(String roleKey) {
        return hasRole(roleKey, null, null);
    }

    /**
     * 检查是否拥有任意一个权限
     *
     * @param permissions    多个权限，用逗号分隔
     * @param roleScope      角色范围
     * @param defaultRoleKey 默认角色
     * @return 是否拥有任意一个权限
     */
    public boolean hasAnyPermissions(String permissions, String roleScope, String defaultRoleKey) {
        return authorizationApplication.hasAnyPermissions(HeaderUtils.getUserId(), permissions, roleScope, defaultRoleKey);
    }

    public boolean hasAnyPermissions(String permissions, String defaultRoleKey) {
        return hasAnyPermissions(permissions, null, defaultRoleKey);
    }

    public boolean hasAnyPermissions(String permissions) {
        return hasAnyPermissions(permissions, null, null);
    }

    /**
     * 检查是否拥有任意一个角色
     *
     * @param roleKeys       多个角色，用逗号分隔
     * @param roleScope      角色范围
     * @param defaultRoleKey 默认角色
     * @return 是否拥有任意一个角色
     */
    public boolean hasAnyRoles(String roleKeys, String roleScope, String defaultRoleKey) {
        return authorizationApplication.hasAnyRoles(HeaderUtils.getUserId(), roleKeys, roleScope, defaultRoleKey);
    }

    public boolean hasAnyRoles(String roleKeys, String defaultRoleKey) {
        return hasAnyRoles(roleKeys, null, defaultRoleKey);
    }

    public boolean hasAnyRoles(String roleKeys) {
        return hasAnyRoles(roleKeys, null, null);
    }
}
