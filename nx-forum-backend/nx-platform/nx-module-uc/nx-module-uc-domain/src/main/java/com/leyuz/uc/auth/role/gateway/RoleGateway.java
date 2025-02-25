package com.leyuz.uc.auth.role.gateway;

import com.leyuz.uc.auth.role.RoleE;

import java.util.List;

public interface RoleGateway {
    RoleE getById(Integer roleId);

    List<RoleE> listByUserId(Long userId);

    boolean save(RoleE role);

    boolean update(RoleE role);

    boolean removeById(Integer roleId);

    List<RoleE> list();

    boolean existsByRoleKey(String roleKey);
} 