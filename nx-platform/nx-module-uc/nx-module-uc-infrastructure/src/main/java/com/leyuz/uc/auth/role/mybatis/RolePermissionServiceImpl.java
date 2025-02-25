package com.leyuz.uc.auth.role.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyuz.common.utils.BaseEntityUtils;
import com.leyuz.uc.auth.role.RolePermissionPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermissionPO> implements IRolePermissionService {

    @Override
    public List<RolePermissionPO> listByRoleKey(String roleKey) {
        LambdaQueryWrapper<RolePermissionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermissionPO::getRoleKey, roleKey)
                .eq(RolePermissionPO::getIsDeleted, false);
        return list(wrapper);
    }

    @Override
    public boolean removeByRoleKey(String roleKey) {
        // 创建一个更新对象，设置逻辑删除标记
        RolePermissionPO updatePO = new RolePermissionPO();
        updatePO.setIsDeleted(true);
        BaseEntityUtils.setUpdateBaseEntity(updatePO);

        // 创建更新条件
        LambdaQueryWrapper<RolePermissionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermissionPO::getRoleKey, roleKey)
                .eq(RolePermissionPO::getIsDeleted, false);

        // 执行逻辑删除
        return update(updatePO, wrapper);
    }
} 