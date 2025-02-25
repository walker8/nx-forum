package com.leyuz.uc.auth.permission.mybatis;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leyuz.uc.auth.permission.PermissionPO;

import java.util.List;

public interface IPermissionService extends IService<PermissionPO> {
    List<PermissionPO> listByRoleKey(String roleKey);

    PermissionPO getByPerms(String perms);

    List<String> listPermsByIds(List<Long> permIds);

    List<String> listPermsByRoleKey(String roleKey);
} 