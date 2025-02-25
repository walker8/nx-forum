package com.leyuz.bbs.admin;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.forum.ForumApplication;
import com.leyuz.bbs.forum.dto.ForumUserRoleCmd;
import com.leyuz.bbs.forum.dto.ForumUserRoleQuery;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.uc.auth.role.dto.UserRoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 权限管理接口
 * </p>
 *
 * @author walker
 * @since 2024-09-16
 */
@Tag(name = "用户权限后台管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/auth")
public class AdminAuthController {
    private final ForumApplication forumApplication;

    @Operation(summary = "分页查询角色授权的用户列表")
    @GetMapping("/user-roles")
    @PreAuthorize("@permissionResolver.hasPermission('admin:system:forum')")
    public SingleResponse<CustomPage<UserRoleVO>> queryUserRoles(@ModelAttribute ForumUserRoleQuery query) {
        return SingleResponse.of(forumApplication.queryForumUserRoles(query));
    }

    @Operation(summary = "授权版块里的用户角色")
    @PostMapping("/user-roles/assign")
    @PreAuthorize("@permissionResolver.hasPermission('admin:system:forum')")
    public SingleResponse<Boolean> assign(@RequestBody ForumUserRoleCmd cmd) {
        return SingleResponse.of(forumApplication.assignAuthorization(cmd));
    }

    @Operation(summary = "删除版块里的用户角色")
    @PostMapping("/user-roles/cancel")
    @PreAuthorize("@permissionResolver.hasPermission('admin:system:forum')")
    public SingleResponse<Boolean> cancel(@RequestParam(defaultValue = "0") Integer forumId, @RequestParam String roleKey) {
        return SingleResponse.of(forumApplication.cancelAuthorization(forumId, roleKey));
    }

}
