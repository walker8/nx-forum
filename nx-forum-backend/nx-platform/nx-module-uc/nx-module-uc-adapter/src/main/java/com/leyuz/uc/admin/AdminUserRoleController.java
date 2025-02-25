package com.leyuz.uc.admin;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.common.security.ResultCode;
import com.leyuz.uc.auth.UserRoleApplication;
import com.leyuz.uc.auth.role.dto.UserRoleCreateCmd;
import com.leyuz.uc.auth.role.dto.UserRoleDTO;
import com.leyuz.uc.auth.role.dto.UserRolePageQuery;
import com.leyuz.uc.auth.role.dto.UserRoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户角色管理")
@RestController
@RequestMapping("/v1/uc/admin/user-roles")
@RequiredArgsConstructor
public class AdminUserRoleController {

    private final UserRoleApplication userRoleApplication;

    @Operation(summary = "获取用户角色列表", description = "只返回未过期的角色")
    @GetMapping("/user/{userId}")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse<List<UserRoleDTO>> listByUserId(@PathVariable Long userId) {
        return SingleResponse.of(userRoleApplication.listByUserId(userId));
    }

    @Operation(summary = "分配用户角色")
    @PostMapping
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse<String> assign(@RequestBody UserRoleCreateCmd userRole) {
        return userRoleApplication.assign(userRole) ?
                SingleResponse.of("分配成功") :
                SingleResponse.buildFailure(ResultCode.BUSINESS_ERROR_CODE, "分配失败");
    }

    @Operation(summary = "取消用户角色")
    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse<String> cancel(@PathVariable Long id) {
        return userRoleApplication.cancel(id) ?
                SingleResponse.of("取消成功") :
                SingleResponse.buildFailure(ResultCode.BUSINESS_ERROR_CODE, "取消失败");
    }


    @Operation(summary = "分页查询角色授权的用户列表")
    @GetMapping("/")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse<CustomPage<UserRoleVO>> queryUserRoles(@ModelAttribute UserRolePageQuery query) {
        return SingleResponse.of(userRoleApplication.pageUserRoles(query));
    }
} 