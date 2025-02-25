package com.leyuz.uc.auth;

import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.uc.auth.permission.PermissionApplication;
import com.leyuz.uc.auth.role.RoleApplication;
import com.leyuz.uc.auth.role.dto.RoleDTO;
import com.leyuz.uc.auth.role.dto.UserRoleCreateCmd;
import com.leyuz.uc.auth.role.dto.UserRolePageQuery;
import com.leyuz.uc.auth.role.dto.UserRoleVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 授权应用服务
 * 负责权限检查、角色检查、用户角色管理等授权相关功能
 *
 * @author walker
 * @since 2024-01-06
 */
@Service
@RequiredArgsConstructor
public class AuthorizationApplication {
    private final PermissionApplication permissionApplication;
    private final UserRoleApplication userRoleApplication;
    private final RoleApplication roleApplication;

    public boolean hasPermission(Long userId, String permission, String roleScope, String defaultRoleKey) {
        if (StringUtils.isBlank(permission)) {
            return true;
        }
        List<String> roleKeys = userRoleApplication.getHighestPriorityRoles(userId, roleScope, defaultRoleKey);
        for (String roleKey : roleKeys) {
            List<String> permsList = permissionApplication.listPermsByRoleKey(roleKey);
            if (permsList.contains(permission)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasRole(Long userId, String roleKey, String roleScope, String defaultRoleKey) {
        if (StringUtils.isBlank(roleKey)) {
            return true;
        }
        List<String> roleKeys = userRoleApplication.getHighestPriorityRoles(userId, roleScope, defaultRoleKey);
        return roleKeys.contains(roleKey);
    }

    /**
     * 检查是否拥有任意一个权限
     *
     * @param userId         用户ID
     * @param permissions    多个权限，用逗号分隔
     * @param roleScope      角色范围
     * @param defaultRoleKey 默认角色
     * @return 是否拥有任意一个权限
     */
    public boolean hasAnyPermissions(Long userId, String permissions, String roleScope, String defaultRoleKey) {
        if (StringUtils.isBlank(permissions)) {
            return true;
        }
        List<String> roleKeys = userRoleApplication.getHighestPriorityRoles(userId, roleScope, defaultRoleKey);
        List<String> permissionList = Arrays.asList(permissions.split(","));

        for (String roleKey : roleKeys) {
            List<String> permsList = permissionApplication.listPermsByRoleKey(roleKey);
            if (permissionList.stream().map(String::trim).anyMatch(permsList::contains)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否拥有任意一个角色
     *
     * @param userId         用户ID
     * @param roleKeys       多个角色，用逗号分隔
     * @param roleScope      角色范围
     * @param defaultRoleKey 默认角色
     * @return 是否拥有任意一个角色
     */
    public boolean hasAnyRoles(Long userId, String roleKeys, String roleScope, String defaultRoleKey) {
        if (StringUtils.isBlank(roleKeys)) {
            return true;
        }
        List<String> userRoleKeys = userRoleApplication.getHighestPriorityRoles(userId, roleScope, defaultRoleKey);
        return Arrays.stream(roleKeys.split(","))
                .map(String::trim).anyMatch(userRoleKeys::contains);
    }

    /**
     * 根据用户ID、角色范围和默认角色键查询权限集合
     *
     * @param userId         用户ID，用于确定用户的角色
     * @param roleScope      角色范围，用于筛选用户的角色
     * @param defaultRoleKey 默认角色键，当用户没有匹配的角色时使用
     * @return 返回用户权限的集合
     */
    public Set<String> queryPermissions(Long userId, String roleScope, String defaultRoleKey) {
        // 初始化权限集合
        Set<String> permissions = new HashSet<>(16);

        // 获取用户最高优先级的角色列表
        List<String> roleKeys = userRoleApplication.getHighestPriorityRoles(userId, roleScope, defaultRoleKey);

        // 遍历每个角色，获取并合并权限列表
        for (String roleKey : roleKeys) {
            // 根据角色键获取权限列表
            List<String> permsList = permissionApplication.listPermsByRoleKey(roleKey);
            // 将当前角色的权限添加到总的权限集合中
            permissions.addAll(permsList);
        }

        // 返回合并后的权限集合
        return permissions;
    }

    /**
     * 获取所有权限
     *
     * @return 所有权限
     */
    public Set<String> listAllPerms() {
        return new HashSet<>(permissionApplication.listPerms());
    }

    public Map<String, String> listAllPermsMap() {
        return permissionApplication.listAllPermsMap();
    }

    /**
     * 获取所有业务角色
     *
     * @return
     */
    public List<RoleDTO> listAllRoles() {
        List<String> excludeRoleKeys = Arrays.asList("UC_ADMIN");
        return roleApplication.listRoles().stream()
                .filter(roleDTO -> roleDTO.getRoleStatus() == 0)
                .filter(roleDTO -> !excludeRoleKeys.contains(roleDTO.getRoleKey()))
                .toList();
    }

    /**
     * 根据用户ID、角色范围和默认角色键查询业务角色集合
     *
     * @param userId
     * @param roleScope
     * @param defaultRoleKey
     * @return
     */
    public List<String> getHighestPriorityRoles(Long userId, String roleScope, String defaultRoleKey) {
        return userRoleApplication.getHighestPriorityRoles(userId, roleScope, defaultRoleKey);
    }

    /**
     * 分页查询用户角色列表
     *
     * @param pageQuery 分页查询参数
     * @return 分页结果
     */
    public CustomPage<UserRoleVO> queryUserRoles(UserRolePageQuery pageQuery) {
        return userRoleApplication.pageUserRoles(pageQuery);
    }

    public boolean assignAuthorization(UserRoleCreateCmd userRoleCreateCmd) {
        return userRoleApplication.assign(userRoleCreateCmd);
    }

    public boolean cancelAuthorization(Long userId, String roleScope, String roleKey) {
        return userRoleApplication.cancel(userId, roleScope, roleKey);
    }
}

