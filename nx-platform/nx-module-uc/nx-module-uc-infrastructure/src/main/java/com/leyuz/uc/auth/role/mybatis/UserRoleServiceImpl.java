package com.leyuz.uc.auth.role.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyuz.common.mybatis.PageQuery;
import com.leyuz.common.utils.BaseEntityUtils;
import com.leyuz.uc.auth.role.UserRolePO;
import com.leyuz.uc.auth.role.mybatis.mapper.UserRoleMapper;
import com.leyuz.uc.user.UserPO;
import com.leyuz.uc.user.mybatis.IUserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRolePO> implements IUserRoleService {

    private final IUserService userService;

    @Override
    public List<UserRolePO> listByUserId(Long userId) {
        LambdaQueryWrapper<UserRolePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRolePO::getUserId, userId)
                .eq(UserRolePO::getIsDeleted, false);
        return list(wrapper);
    }

    @Override
    public List<UserRolePO> listValidRolesByUserId(Long userId) {
        LambdaQueryWrapper<UserRolePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRolePO::getUserId, userId)
                .eq(UserRolePO::getIsDeleted, false)
                .and(w -> w.isNull(UserRolePO::getExpireTime)
                        .or()
                        .gt(UserRolePO::getExpireTime, LocalDateTime.now()));
        return list(wrapper);
    }

    @Override
    public boolean removeById(Long id) {
        UserRolePO entity = new UserRolePO();
        entity.setId(id);
        entity.setIsDeleted(true);
        BaseEntityUtils.setUpdateBaseEntity(entity);
        return updateById(entity);
    }

    @Override
    public List<UserRolePO> listByRoleKey(String roleKey) {
        LambdaQueryWrapper<UserRolePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRolePO::getRoleKey, roleKey)
                .eq(UserRolePO::getIsDeleted, false)
                .orderByDesc(UserRolePO::getCreateTime);
        return list(wrapper);
    }

    @Override
    public Page<UserRolePO> queryUserRoles(PageQuery pageQuery) {
        Page<UserRolePO> page = new Page<>(pageQuery.getPageNo(), pageQuery.getPageSize());

        LambdaQueryWrapper<UserRolePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRolePO::getIsDeleted, false);

        // 角色标识查询条件
        String roleKey = (String) pageQuery.getParams().get("roleKey");
        if (StringUtils.isNotBlank(roleKey)) {
            wrapper.eq(UserRolePO::getRoleKey, roleKey);
        }

        String roleScope = (String) pageQuery.getParams().get("roleScope");
        if (StringUtils.isNotBlank(roleScope)) {
            wrapper.eq(UserRolePO::getRoleScope, roleScope);
        }

        // 用户名查询条件
        String userName = (String) pageQuery.getParams().get("userName");
        if (StringUtils.isNotBlank(userName)) {
            // 先查询匹配用户名的用户ID列表
            LambdaQueryWrapper<UserPO> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.like(UserPO::getUserName, userName);
            List<Long> userIds = userService.list(userWrapper)
                    .stream()
                    .map(UserPO::getUserId)
                    .collect(Collectors.toList());

            if (!userIds.isEmpty()) {
                wrapper.in(UserRolePO::getUserId, userIds);
            } else {
                // 用户名不存在返回空结果
                return page;
            }
        }

        Long userId = (Long) pageQuery.getParams().get("userId");
        if (userId != null && userId > 0) {
            wrapper.eq(UserRolePO::getUserId, userId);
        }

        wrapper.orderByDesc(UserRolePO::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public boolean cancel(Long userId, String roleScope, String roleKey) {
        LambdaQueryWrapper<UserRolePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRolePO::getUserId, userId)
                .eq(UserRolePO::getIsDeleted, false);
        if (StringUtils.isNotBlank(roleKey)) {
            wrapper.eq(UserRolePO::getRoleKey, roleKey);
        }
        if (StringUtils.isNotBlank(roleScope)) {
            wrapper.eq(UserRolePO::getRoleScope, roleScope);
        } else {
            wrapper.eq(UserRolePO::getRoleScope, "");
        }
        return remove(wrapper);
    }
}