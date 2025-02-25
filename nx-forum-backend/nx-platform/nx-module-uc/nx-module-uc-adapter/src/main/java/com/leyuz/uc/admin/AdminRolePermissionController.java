package com.leyuz.uc.admin;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.common.security.ResultCode;
import com.leyuz.uc.auth.RolePermissionApplication;
import com.leyuz.uc.auth.role.dto.RolePermissionCreateCmd;
import com.leyuz.uc.auth.role.RolePermissionPO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色权限管理")
@RestController
@RequestMapping("/v1/uc/admin/role-permissions")
@RequiredArgsConstructor
public class AdminRolePermissionController {

    private final RolePermissionApplication rolePermissionApplication;

    @Operation(summary = "获取角色权限列表")
    @GetMapping("/role/{roleKey}")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse<List<RolePermissionPO>> listByRoleKey(@PathVariable String roleKey) {
        return SingleResponse.of(rolePermissionApplication.listByRoleKey(roleKey));
    }

    @Operation(summary = "分配角色权限")
    @PostMapping
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse<String> assign(@RequestBody RolePermissionCreateCmd rolePermissionCreateCmd) {
        return rolePermissionApplication.save(rolePermissionCreateCmd) ?
                SingleResponse.of("分配成功") :
                SingleResponse.buildFailure(ResultCode.BUSINESS_ERROR_CODE, "分配失败");
    }

    @Operation(summary = "取消角色权限")
    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse<String> cancel(@PathVariable Long id) {
        return rolePermissionApplication.removeById(id) ?
                SingleResponse.of("取消成功") :
                SingleResponse.buildFailure(ResultCode.BUSINESS_ERROR_CODE, "取消失败");
    }
} 