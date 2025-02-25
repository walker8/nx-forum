package com.leyuz.bbs.auth;

import com.leyuz.bbs.forum.ForumApplication;
import com.leyuz.bbs.forum.ForumPO;
import com.leyuz.bbs.forum.dto.ForumAccessDTO;
import com.leyuz.common.utils.HeaderUtils;
import com.leyuz.uc.auth.AuthorizationApplication;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ForumPermissionResolver {
    private final AuthorizationApplication authApplication;
    private final ForumApplication forumApplication;
    @Value("${nx.auth.debug:false}")
    private boolean enableAuthDebug;
    private static final List<String> forumAccessPerms = Arrays.asList("thread:view", "thread:new", "comment:new", "forum:visit:section");
    private static final String GLOBAL_ROLE_SCOPE = "ALL";

    /**
     * 获取论坛角色范围
     */
    public static String getForumRoleScope(Integer forumId) {
        return (forumId != null && forumId > 10) ? "FORUM_" + forumId : GLOBAL_ROLE_SCOPE;
    }

    /**
     * 校验权限字符串
     */
    private void validatePermissions(String permissions) {
        if (StringUtils.isEmpty(permissions)) {
            throw new IllegalArgumentException("权限标识不能为空");
        }
    }

    public void checkPermission(Integer forumId, String permission) {
        validatePermissions(permission);
        if (!hasPermission(forumId, permission)) {
            Map<String, String> permsMap = authApplication.listAllPermsMap();
            if (permsMap.get(permission) != null) {
                throw new AuthorizationDeniedException("无" + permsMap.get(permission) + "权限");
            } else {
                throw new AuthorizationDeniedException("无权限操作");
            }
        }
    }

    public void checkPermission(String permission) {
        checkPermission(null, permission);
    }

    public void checkAnyPermissions(Integer forumId, String permissions) {
        validatePermissions(permissions);
        if (!hasAnyPermissions(forumId, permissions)) {
            throw new AuthorizationDeniedException("无权限操作");
        }
    }

    public void checkAnyPermissions(String permissions) {
        checkAnyPermissions(null, permissions);
    }

    public boolean hasPermission(Integer forumId, String permission) {
        String roleScope = getForumRoleScope(forumId);
        ForumPO forumPO = forumApplication.getForumById(forumId);
        if (Boolean.TRUE.equals(!GLOBAL_ROLE_SCOPE.equalsIgnoreCase(roleScope)
                && forumPO != null
                && forumPO.getForumAccess())
                && forumAccessPerms.contains(permission)) {
            // 版块自定义权限优先级最高
            List<ForumAccessDTO> forumAccess = forumApplication.getForumAccess(forumId);
            List<String> roles = authApplication.getHighestPriorityRoles(HeaderUtils.getUserId(), roleScope, getDefaultRoleKey());
            if (CollectionUtils.isEmpty(roles)) {
                return false;
            }
            return roles.stream().anyMatch(roleKey -> {
                for (ForumAccessDTO forumAccessDTO : forumAccess) {
                    if (forumAccessDTO.getRoleKey().equals(roleKey)) {
                        return forumAccessDTO.getPerms().contains(permission);
                    }
                }
                return false;
            });
        }
        return authApplication.hasPermission(HeaderUtils.getUserId(), permission, roleScope, getDefaultRoleKey());
    }

    public boolean hasPermission(String permission) {
        return hasPermission(null, permission);
    }

    /**
     * 判断当前用户是否有任意权限，暂不支持版块自定义权限
     *
     * @param forumId
     * @param permissions
     * @return
     */
    public boolean hasAnyPermissions(Integer forumId, String permissions) {
        String roleScope = getForumRoleScope(forumId);
        return authApplication.hasAnyPermissions(HeaderUtils.getUserId(), permissions, roleScope, getDefaultRoleKey());
    }

    public boolean hasAnyPermissions(String permissions) {
        return hasAnyPermissions(null, permissions);
    }

    public Set<String> queryPermissions(Integer forumId) {
        if (enableAuthDebug) {
            return authApplication.listAllPerms();
        }
        Long userId = HeaderUtils.getUserId();
        String roleScope = getForumRoleScope(forumId);
        Set<String> permissions = authApplication.queryPermissions(userId, roleScope, getDefaultRoleKey());
        ForumPO forumPO = forumApplication.getForumById(forumId);
        if (forumPO == null) {
            return permissions;
        }
        if (!GLOBAL_ROLE_SCOPE.equalsIgnoreCase(roleScope) && Boolean.TRUE.equals(forumPO.getForumAccess())) {
            // 版块自定义权限优先级最高
            forumAccessPerms.forEach(permissions::remove);
            List<ForumAccessDTO> forumAccess = forumApplication.getForumAccess(forumId);
            List<String> roles = authApplication.getHighestPriorityRoles(userId, roleScope, getDefaultRoleKey());
            for (String role : roles) {
                for (ForumAccessDTO forumAccessDTO : forumAccess) {
                    if (forumAccessDTO.getRoleKey().equals(role)) {
                        permissions.addAll(forumAccessDTO.getPerms());
                    }
                }
            }
        }
        return permissions;
    }

    private String getDefaultRoleKey() {
        Long userId = HeaderUtils.getUserId();
        if (userId != null && userId > 0) {
            // 获取登录用户默认角色
            return RoleConstant.ROLE_KEY_USER;
        } else {
            // 获取游客默认角色
            return RoleConstant.ROLE_KEY_GUEST;
        }
    }
}
