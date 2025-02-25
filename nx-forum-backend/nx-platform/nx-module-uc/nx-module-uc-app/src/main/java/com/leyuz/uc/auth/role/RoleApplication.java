package com.leyuz.uc.auth.role;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.common.exception.ValidationException;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.common.mybatis.DataBaseUtils;
import com.leyuz.common.mybatis.PageQuery;
import com.leyuz.module.cache.GenericCache;
import com.leyuz.uc.auth.RolePermissionApplication;
import com.leyuz.uc.auth.permission.PermissionMapper;
import com.leyuz.uc.auth.role.dataobject.RoleStatusV;
import com.leyuz.uc.auth.role.dto.*;
import com.leyuz.uc.auth.role.gateway.RoleGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RoleApplication {

    private final RoleGateway roleGateway;
    private final RoleMapper roleMapper;
    private final RolePermissionApplication rolePermissionApplication;
    private final PermissionMapper permissionMapper;
    private final GenericCache<String, List<RoleDTO>> roleListCache;

    private static final Pattern ROLE_KEY_PATTERN = Pattern.compile("^[a-zA-Z]\\w{0,99}$");

    private void validateRoleKey(String roleKey) {
        if (roleKey == null || roleKey.isEmpty()) {
            throw new ValidationException("角色标识不能为空");
        }
        if (!ROLE_KEY_PATTERN.matcher(roleKey).matches()) {
            throw new ValidationException("角色标识只能由字母、数字和下划线组成");
        }

        if (roleGateway.existsByRoleKey(roleKey)) {
            throw new ValidationException("角色标识已存在");
        }
    }

    public RoleDTO getRole(Integer roleId) {
        RoleE roleE = roleGateway.getById(roleId);
        if (roleE == null) {
            return null;
        }

        RoleDTO roleDTO = convertToDTO(roleE);

        // 获取角色的权限列表
        List<RolePermissionPO> rolePermissions = rolePermissionApplication.listByRoleKey(roleE.getRoleKey());
        if (!CollectionUtils.isEmpty(rolePermissions)) {
            List<Long> permIds = rolePermissions.stream()
                    .map(RolePermissionPO::getPermId)
                    .toList();

            // 获取权限标识列表
            List<String> perms = permissionMapper.listPermsByIds(permIds);
            roleDTO.setPerms(perms);
        }

        return roleDTO;
    }

    public List<RoleDTO> listRoles() {
        return roleListCache.computeIfAbsent("all", (key) ->
                roleGateway.list().stream()
                        .map(this::convertToDTO)
                        .toList()
        );
    }

    @Transactional
    public boolean createRole(RoleCreateCmd cmd) {
        validateRoleKey(cmd.getRoleKey());

        RoleE roleE = RoleE.builder()
                .roleName(cmd.getRoleName())
                .roleKey(cmd.getRoleKey())
                .roleStatus(RoleStatusV.NORMAL)
                .remark(cmd.getRemark())
                .priority(cmd.getPriority())
                .build();

        boolean saved = roleGateway.save(roleE);

        // 保存角色权限关系
        if (saved && !CollectionUtils.isEmpty(cmd.getPerms())) {
            for (String perm : cmd.getPerms()) {
                RolePermissionCreateCmd rolePermCmd = new RolePermissionCreateCmd();
                rolePermCmd.setRoleKey(cmd.getRoleKey());
                rolePermCmd.setPerms(perm);
                rolePermissionApplication.save(rolePermCmd);
            }
        }
        roleListCache.remove("all");

        return saved;
    }

    @Transactional
    public boolean updateRole(RoleUpdateCmd cmd) {
        RoleE oldRole = roleGateway.getById(cmd.getRoleId());
        if (oldRole == null) {
            return false;
        }

        RoleE roleE = RoleE.builder()
                .roleId(oldRole.getRoleId())
                .roleName(cmd.getRoleName())
                .roleStatus(RoleStatusV.of(cmd.getRoleStatus()))
                .remark(cmd.getRemark())
                .priority(cmd.getPriority())
                .build();

        boolean updated = roleGateway.update(roleE);

        // 更新角色权限关系
        if (updated && cmd.getPerms() != null) {
            // 先删除原有权限
            rolePermissionApplication.removeByRoleKey(oldRole.getRoleKey());

            // 添加新的权限
            for (String perm : cmd.getPerms()) {
                RolePermissionCreateCmd rolePermCmd = new RolePermissionCreateCmd();
                rolePermCmd.setRoleKey(oldRole.getRoleKey());
                rolePermCmd.setPerms(perm);
                rolePermissionApplication.save(rolePermCmd);
            }
        }
        roleListCache.remove("all");

        return updated;
    }

    @Transactional
    public boolean deleteRole(Integer roleId) {
        return roleGateway.removeById(roleId);
    }

    public CustomPage<RoleDTO> queryRoles(RolePageQuery query) {
        PageQuery pageQuery = PageQuery.builder()
                .pageNo(query.getPageNo())
                .pageSize(query.getPageSize())
                .build();
        Map<String, Object> params = pageQuery.getParams();
        params.put("roleName", query.getRoleName());
        params.put("roleStatus", query.getRoleStatus());
        Page<RolePO> page = new Page<>(pageQuery.getPageNo(), pageQuery.getPageSize());
        Page<RolePO> rolePage = roleMapper.queryRoles(page, params);

        return DataBaseUtils.createCustomPage(rolePage, this::convertPOToDTO);
    }

    private RoleDTO convertPOToDTO(RolePO rolePO) {
        // 将rolePO转换成RoleDTO
        return RoleDTO.builder()
                .roleId(rolePO.getRoleId())
                .roleName(rolePO.getRoleName())
                .roleKey(rolePO.getRoleKey())
                .roleStatus(rolePO.getRoleStatus())
                .remark(rolePO.getRemark())
                .priority(rolePO.getPriority())
                .build();
    }

    private RoleDTO convertToDTO(RoleE roleE) {
        if (roleE == null) {
            return null;
        }
        RoleDTO dto = new RoleDTO();
        dto.setRoleId(roleE.getRoleId());
        dto.setRoleName(roleE.getRoleName());
        dto.setRoleKey(roleE.getRoleKey());
        dto.setRoleStatus(roleE.getRoleStatus().getValue());
        dto.setRoleStatusDesc(roleE.getRoleStatus().getDesc());
        dto.setRemark(roleE.getRemark());
        dto.setPriority(roleE.getPriority());
        return dto;
    }
} 