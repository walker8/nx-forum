package com.leyuz.uc.auth.role.mybatis;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyuz.common.mybatis.PageQuery;
import com.leyuz.uc.auth.role.RolePO;
import com.leyuz.uc.auth.role.mybatis.mapper.RoleMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, RolePO> implements IRoleService {

    private final RoleMapper roleMapper;

    @Override
    public List<RolePO> listByUserId(Long userId) {
        return roleMapper.selectListByUserId(userId);
    }

    @Override
    public Page<RolePO> queryRoles(PageQuery pageQuery) {
        Page<RolePO> page = new Page<>(pageQuery.getPageNo(), pageQuery.getPageSize());
        QueryWrapper<RolePO> queryWrapper = new QueryWrapper<>();
        Map<String, Object> params = pageQuery.getParams();
        String roleName = MapUtil.getStr(params, "roleName");
        Integer roleStatus = MapUtil.getInt(params, "roleStatus", null);
        if (StringUtils.isNotBlank(roleName)) {
            queryWrapper.like("role_name", roleName);

        }
        if (roleStatus != null && roleStatus > 0) {
            queryWrapper.eq("role_status", roleStatus);
        }
        return roleMapper.selectPage(page, queryWrapper);
    }
}