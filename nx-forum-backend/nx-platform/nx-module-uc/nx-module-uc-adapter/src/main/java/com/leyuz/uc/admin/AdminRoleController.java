package com.leyuz.uc.admin;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.common.security.ResultCode;
import com.leyuz.uc.auth.role.RoleApplication;
import com.leyuz.uc.auth.role.dto.RoleCreateCmd;
import com.leyuz.uc.auth.role.dto.RoleDTO;
import com.leyuz.uc.auth.role.dto.RolePageQuery;
import com.leyuz.uc.auth.role.dto.RoleUpdateCmd;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "角色管理")
@RestController
@RequestMapping("/v1/uc/admin/roles")
@RequiredArgsConstructor
public class AdminRoleController {

    private final RoleApplication roleApplication;

    @Operation(summary = "查询角色列表")
    @GetMapping
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse<CustomPage<RoleDTO>> queryRoles(@RequestParam(defaultValue = "") String roleName,
                                                          @RequestParam(defaultValue = "") Integer roleStatus,
                                                          @RequestParam(defaultValue = "1") int pageNo,
                                                          @RequestParam(defaultValue = "10") int pageSize) {
        RolePageQuery query = RolePageQuery.builder().roleName(roleName)
                .roleStatus(roleStatus).pageNo(pageNo).pageSize(pageSize).build();
        CustomPage<RoleDTO> roleRespPage = roleApplication.queryRoles(query);
        return SingleResponse.of(roleRespPage);
    }

    @Operation(summary = "获取角色详情")
    @GetMapping("/{roleId}")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse<RoleDTO> getInfo(@PathVariable Integer roleId) {
        return SingleResponse.of(roleApplication.getRole(roleId));
    }

    @Operation(summary = "新增角色")
    @PostMapping
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse<String> add(@RequestBody RoleCreateCmd role) {
        return roleApplication.createRole(role) ?
                SingleResponse.of("新增成功") :
                SingleResponse.buildFailure(ResultCode.BUSINESS_ERROR_CODE, "新增失败");
    }

    @Operation(summary = "修改角色")
    @PutMapping
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse<String> edit(@RequestBody RoleUpdateCmd role) {
        return roleApplication.updateRole(role) ?
                SingleResponse.of("修改成功") :
                SingleResponse.buildFailure(ResultCode.BUSINESS_ERROR_CODE, "修改失败");
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{roleId}")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse<String> remove(@PathVariable Integer roleId) {
        return roleApplication.deleteRole(roleId) ?
                SingleResponse.of("删除成功") :
                SingleResponse.buildFailure(ResultCode.BUSINESS_ERROR_CODE, "删除失败");
    }
} 