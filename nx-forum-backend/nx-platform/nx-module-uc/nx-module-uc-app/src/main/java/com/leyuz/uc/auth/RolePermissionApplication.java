package com.leyuz.uc.auth;

import com.leyuz.common.exception.ValidationException;
import com.leyuz.common.utils.BaseEntityUtils;
import com.leyuz.uc.auth.permission.PermissionMapper;
import com.leyuz.uc.auth.permission.PermissionPO;
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
        RolePermissionPO rolePermissionPO = new RolePermissionPO();
        rolePermissionPO.setRoleKey(rolePermissionCreateCmd.getRoleKey());
        PermissionPO permissionPO = permissionMapper.getByPerms(perms);
        if (permissionPO == null) {
            throw new ValidationException("资源不存在");
        }
        rolePermissionPO.setPermId(permissionPO.getPermId());
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
