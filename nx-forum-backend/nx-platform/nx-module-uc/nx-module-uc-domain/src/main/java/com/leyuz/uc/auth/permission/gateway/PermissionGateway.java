package com.leyuz.uc.auth.permission.gateway;

import com.leyuz.uc.auth.permission.PermissionE;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PermissionGateway {
    Optional<PermissionE> getById(Long permId);

    List<PermissionE> listByRoleKey(String roleKey);

    List<PermissionE> listByRoleKeys(List<String> roleKeys);

    boolean save(PermissionE permission);

    boolean update(PermissionE permission);

    boolean removeById(Long permId);

    List<PermissionE> list();

    boolean existsByPerms(String perms);

    List<PermissionE> listByParentId(Long parentId);

    List<String> listPermsByRoleKey(String roleKey);

    List<String> listAllPerms();

    Map<String, String> listAllPermsMap();
} 