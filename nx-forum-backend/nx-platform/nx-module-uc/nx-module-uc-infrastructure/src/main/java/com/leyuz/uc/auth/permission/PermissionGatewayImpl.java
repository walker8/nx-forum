package com.leyuz.uc.auth.permission;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.leyuz.common.utils.BaseEntityUtils;
import com.leyuz.uc.auth.permission.dataobject.PermissionStatusV;
import com.leyuz.uc.auth.permission.dataobject.PermissionTypeV;
import com.leyuz.uc.auth.permission.gateway.PermissionGateway;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PermissionGatewayImpl implements PermissionGateway {

    private final PermissionMapper permissionMapper;

    @Override
    public Optional<PermissionE> getById(Long permId) {
        PermissionPO po = permissionMapper.selectById(permId);
        return Optional.ofNullable(po).map(this::convert);
    }

    @Override
    public List<PermissionE> listByRoleKey(String roleKey) {
        List<PermissionPO> permissionPOList = permissionMapper.selectListByRoleKey(roleKey);
        return permissionPOList.stream().map(this::convert).toList();
    }

    @Override
    public List<PermissionE> listByRoleKeys(List<String> roleKeys) {
        if (roleKeys == null || roleKeys.isEmpty()) {
            return Collections.emptyList();
        }
        return roleKeys.stream()
                .flatMap(roleKey -> listByRoleKey(roleKey).stream())
                .distinct()
                .toList();
    }

    @Override
    public boolean save(PermissionE permission) {
        PermissionPO permissionPO = convertToPO(permission);
        BaseEntityUtils.setCreateBaseEntity(permissionPO);
        return permissionMapper.insert(permissionPO) > 0;
    }

    @Override
    public boolean update(PermissionE permission) {
        PermissionPO permissionPO = convertToPO(permission);
        BaseEntityUtils.setUpdateBaseEntity(permissionPO);
        return permissionMapper.updateById(permissionPO) > 0;
    }

    @Override
    public boolean removeById(Long permId) {
        return getById(permId)
                .map(permission -> {
                    PermissionPO po = convertToPO(permission);
                    BaseEntityUtils.setUpdateBaseEntity(po);
                    po.setIsDeleted(true);
                    return permissionMapper.updateById(po) > 0;
                })
                .orElse(false);
    }

    @Override
    public List<PermissionE> list() {
        LambdaQueryWrapper<PermissionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PermissionPO::getIsDeleted, false);
        return permissionMapper.selectList(wrapper).stream()
                .map(this::convert)
                .toList();
    }

    @Override
    public boolean existsByPerms(String perms) {
        if (perms == null || perms.isEmpty()) {
            return false;
        }
        LambdaQueryWrapper<PermissionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PermissionPO::getPerms, perms)
                .eq(PermissionPO::getIsDeleted, false);
        return permissionMapper.selectCount(wrapper) > 0;
    }

    @Override
    public List<PermissionE> listByParentId(Long parentId) {
        LambdaQueryWrapper<PermissionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PermissionPO::getParentId, parentId)
                .eq(PermissionPO::getIsDeleted, false);
        return permissionMapper.selectList(wrapper).stream()
                .map(this::convert)
                .toList();
    }

    @Override
    public List<String> listPermsByRoleKey(String roleKey) {
        return permissionMapper.selectPermsByRoleKey(roleKey);
    }

    @Override
    public List<String> listAllPerms() {
        List<PermissionE> permissions = list();
        return permissions.stream()
                .map(PermissionE::getPerms)
                .filter(StringUtils::isNotEmpty)
                .distinct()
                .toList();
    }

    @Override
    public Map<String, String> listAllPermsMap() {
        List<PermissionE> permissions = list();
        return permissions.stream()
                .filter(p -> StringUtils.isNotEmpty(p.getPerms()))
                .collect(Collectors.toMap(PermissionE::getPerms, PermissionE::getPermName));
    }

    private PermissionE convert(PermissionPO po) {
        if (po == null) {
            return null;
        }
        return PermissionE.builder()
                .permId(po.getPermId())
                .parentId(po.getParentId())
                .permName(po.getPermName())
                .perms(po.getPerms())
                .remark(po.getRemark())
                .permStatus(PermissionStatusV.of(po.getPermStatus()))
                .permType(PermissionTypeV.of(po.getPermType()))
                .permOrder(po.getPermOrder())
                .build();
    }

    private PermissionPO convertToPO(PermissionE entity) {
        if (entity == null) {
            return null;
        }
        PermissionPO po = new PermissionPO();
        po.setPermId(entity.getPermId());
        po.setParentId(entity.getParentId());
        po.setPermName(entity.getPermName());
        po.setPerms(entity.getPerms());
        po.setRemark(entity.getRemark());
        po.setPermStatus(entity.getPermStatus().getCode());
        po.setPermType(entity.getPermType().getCode());
        po.setPermOrder(entity.getPermOrder());
        po.setIsDeleted(false);
        return po;
    }
} 