package com.leyuz.uc.auth.role;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.leyuz.common.utils.BaseEntityUtils;
import com.leyuz.uc.auth.role.mybatis.IRoleService;
import com.leyuz.uc.domain.auth.role.RoleE;
import com.leyuz.uc.domain.auth.role.dataobject.RoleStatusV;
import com.leyuz.uc.domain.auth.role.gateway.RoleGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RoleGatewayImpl implements RoleGateway {

    private final IRoleService roleService;

    @Override
    public RoleE getById(Integer roleId) {
        RolePO rolePO = roleService.getById(roleId);
        return convert(rolePO);
    }

    @Override
    public List<RoleE> listByUserId(Long userId) {
        List<RolePO> rolePOList = roleService.listByUserId(userId);
        return rolePOList.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public boolean save(RoleE role) {
        RolePO rolePO = new RolePO();
        rolePO.setRoleName(role.getRoleName());
        rolePO.setRoleKey(role.getRoleKey());
        rolePO.setRemark(role.getRemark());
        rolePO.setRoleStatus(role.getRoleStatus().getValue());
        rolePO.setPriority(role.getPriority());
        BaseEntityUtils.setCreateBaseEntity(rolePO);
        return roleService.save(rolePO);
    }

    @Override
    public boolean update(RoleE role) {
        RolePO rolePO = new RolePO();
        rolePO.setRoleId(role.getRoleId());
        rolePO.setRoleName(role.getRoleName());
        rolePO.setRoleKey(role.getRoleKey());
        rolePO.setRemark(role.getRemark());
        rolePO.setRoleStatus(role.getRoleStatus().getValue());
        rolePO.setPriority(role.getPriority());
        BaseEntityUtils.setUpdateBaseEntity(rolePO);
        return roleService.updateById(rolePO);
    }

    @Override
    public boolean removeById(Integer roleId) {
        // 逻辑删除
        RolePO rolePO = new RolePO();
        rolePO.setRoleId(roleId);
        rolePO.setIsDeleted(true);
        BaseEntityUtils.setUpdateBaseEntity(rolePO);
        return roleService.updateById(rolePO);
    }

    @Override
    public List<RoleE> list() {
        LambdaQueryWrapper<RolePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePO::getIsDeleted, false);
        return roleService.list(wrapper).stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByRoleKey(String roleKey) {
        LambdaQueryWrapper<RolePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePO::getRoleKey, roleKey)
                .eq(RolePO::getIsDeleted, false);
        return roleService.count(wrapper) > 0;
    }

    private RoleE convert(RolePO rolePO) {
        if (rolePO == null) {
            return null;
        }
        return RoleE.builder()
                .roleId(rolePO.getRoleId())
                .priority(rolePO.getPriority())
                .roleName(rolePO.getRoleName())
                .roleKey(rolePO.getRoleKey())
                .remark(rolePO.getRemark())
                .roleStatus(RoleStatusV.of(rolePO.getRoleStatus()))
                .build();
    }
} 