package com.leyuz.uc.auth;

import com.leyuz.common.exception.ValidationException;
import com.leyuz.common.utils.BaseEntityUtils;
import com.leyuz.uc.auth.permission.PermissionMapper;
import com.leyuz.uc.auth.role.RolePermissionMapper;
import com.leyuz.uc.auth.role.RolePermissionPO;
import com.leyuz.uc.auth.role.dto.RolePermissionCreateCmd;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolePermissionApplication {
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;

    public boolean removeById(Long id) {
        return rolePermissionMapper.deleteById(id) > 0;
    }

    public boolean save(RolePermissionCreateCmd rolePermissionCreateCmd) {
        String perms = rolePermissionCreateCmd.getPerms();

        // 校验权限标识是否存在
        if (permissionMapper.getByPerms(perms) == null) {
            throw new ValidationException("资源不存在");
        }

        RolePermissionPO rolePermissionPO = new RolePermissionPO();
        rolePermissionPO.setRoleKey(rolePermissionCreateCmd.getRoleKey());
        rolePermissionPO.setPerms(perms);
        BaseEntityUtils.setCreateBaseEntity(rolePermissionPO);
        return rolePermissionMapper.insert(rolePermissionPO) > 0;
    }

    public List<RolePermissionPO> listByRoleKey(String roleKey) {
        return rolePermissionMapper.listByRoleKey(roleKey);
    }

    @Transactional
    public boolean removeByRoleKey(String roleKey) {
        return rolePermissionMapper.removeByRoleKey(roleKey) > 0;
    }
}
