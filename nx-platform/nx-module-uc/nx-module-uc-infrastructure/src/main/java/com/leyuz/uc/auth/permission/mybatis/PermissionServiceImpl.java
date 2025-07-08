package com.leyuz.uc.auth.permission.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyuz.uc.auth.permission.PermissionPO;
import com.leyuz.uc.auth.permission.mybatis.mapper.PermissionMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, PermissionPO> implements IPermissionService {

    private final PermissionMapper permissionMapper;

    @Override
    public List<PermissionPO> listByRoleKey(String roleKey) {
        return permissionMapper.selectListByRoleKey(roleKey);
    }

    @Override
    public PermissionPO getByPerms(String perms) {
        LambdaQueryWrapper<PermissionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PermissionPO::getPerms, perms)
                .eq(PermissionPO::getIsDeleted, false)
                .last("LIMIT 1");
        return getOne(wrapper);
    }

    @Override
    public List<String> listPermsByIds(List<Long> permIds) {

        LambdaQueryWrapper<PermissionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(PermissionPO::getPerms)
                .in(PermissionPO::getPermId, permIds)
                .eq(PermissionPO::getIsDeleted, false);

        return list(wrapper).stream()
                .map(PermissionPO::getPerms)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> listPermsByRoleKey(String roleKey) {
        if (StringUtils.isEmpty(roleKey)) {
            return Collections.emptyList();
        }
        List<String> permsList = permissionMapper.selectPermsByRoleKey(roleKey);
        return permsList.stream().distinct().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
    }
}