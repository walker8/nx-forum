package com.leyuz.uc.admin;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.common.security.ResultCode;
import com.leyuz.uc.auth.permission.PermissionApplication;
import com.leyuz.uc.auth.permission.dto.PermissionCreateCmd;
import com.leyuz.uc.auth.permission.dto.PermissionDTO;
import com.leyuz.uc.auth.permission.dto.PermissionUpdateCmd;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "权限管理")
@RestController
@RequestMapping("/v1/uc/admin/permissions")
@RequiredArgsConstructor
public class AdminPermissionController {

    private final PermissionApplication permissionApplication;

    @Operation(summary = "获取权限列表")
    @GetMapping
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse<List<PermissionDTO>> list() {
        return SingleResponse.of(permissionApplication.listPermissions());
    }

    @Operation(summary = "获取角色的权限列表")
    @GetMapping("/role/{roleKey}")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse<List<PermissionDTO>> listByRoleKey(@PathVariable String roleKey) {
        return SingleResponse.of(permissionApplication.listPermissionsByRoleKey(roleKey));
    }

    @Operation(summary = "获取权限详情")
    @GetMapping("/{permId}")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse<PermissionDTO> getInfo(@PathVariable Long permId) {
        return SingleResponse.of(permissionApplication.getPermission(permId));
    }

    @Operation(summary = "新增权限")
    @PostMapping
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse<String> add(@RequestBody PermissionCreateCmd permission) {
        return permissionApplication.createPermission(permission) ?
                SingleResponse.of("新增成功") :
                SingleResponse.buildFailure(ResultCode.BUSINESS_ERROR_CODE, "新增失败");
    }

    @Operation(summary = "修改权限")
    @PutMapping
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse<String> edit(@RequestBody PermissionUpdateCmd permission) {
        return permissionApplication.updatePermission(permission) ?
                SingleResponse.of("修改成功") :
                SingleResponse.buildFailure(ResultCode.BUSINESS_ERROR_CODE, "修改失败");
    }

    @Operation(summary = "删除权限")
    @DeleteMapping("/{permId}")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse<String> remove(@PathVariable Long permId) {
        return permissionApplication.deletePermission(permId) ?
                SingleResponse.of("删除成功") :
                SingleResponse.buildFailure(ResultCode.BUSINESS_ERROR_CODE, "删除失败");
    }

    @Operation(summary = "启用权限")
    @PutMapping("/{permId}/enable")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse<String> enable(@PathVariable Long permId) {
        return permissionApplication.enablePermission(permId) ?
                SingleResponse.of("启用成功") :
                SingleResponse.buildFailure(ResultCode.BUSINESS_ERROR_CODE, "启用失败");
    }

    @Operation(summary = "禁用权限")
    @PutMapping("/{permId}/disable")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse<String> disable(@PathVariable Long permId) {
        return permissionApplication.disablePermission(permId) ?
                SingleResponse.of("禁用成功") :
                SingleResponse.buildFailure(ResultCode.BUSINESS_ERROR_CODE, "禁用失败");
    }
} 