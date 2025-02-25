package com.leyuz.uc.auth;

import com.leyuz.common.exception.ValidationException;
import com.leyuz.common.utils.BaseEntityUtils;
import com.leyuz.uc.auth.dto.RolePermissionCreateCmd;
import com.leyuz.uc.auth.permission.PermissionPO;
import com.leyuz.uc.auth.permission.mybatis.IPermissionService;
import com.leyuz.uc.auth.role.RolePermissionPO;
import com.leyuz.uc.auth.role.mybatis.IRolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolePermissionApplication {
    private final IRolePermissionService rolePermissionService;
    private final IPermissionService permissionService;

    public boolean removeById(Long id) {
        return rolePermissionService.removeById(id);
    }

    public boolean save(RolePermissionCreateCmd rolePermissionCreateCmd) {
        String perms = rolePermissionCreateCmd.getPerms();
        RolePermissionPO rolePermissionPO = new RolePermissionPO();
        rolePermissionPO.setRoleKey(rolePermissionCreateCmd.getRoleKey());
        PermissionPO permissionPO = permissionService.getByPerms(perms);
        if (permissionPO == null) {
            throw new ValidationException("资源不存在");
        }
        rolePermissionPO.setPermId(permissionPO.getPermId());
        BaseEntityUtils.setCreateBaseEntity(rolePermissionPO);
        return rolePermissionService.save(rolePermissionPO);
    }

    public List<RolePermissionPO> listByRoleKey(String roleKey) {
        return rolePermissionService.listByRoleKey(roleKey);
    }

    @Transactional
    public boolean removeByRoleKey(String roleKey) {
        return rolePermissionService.removeByRoleKey(roleKey);
    }
}
