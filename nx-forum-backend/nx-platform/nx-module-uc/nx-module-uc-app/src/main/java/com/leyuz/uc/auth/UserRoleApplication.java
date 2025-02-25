package com.leyuz.uc.auth;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.common.exception.ValidationException;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.common.mybatis.DataBaseUtils;
import com.leyuz.common.mybatis.PageQuery;
import com.leyuz.common.utils.BaseEntityUtils;
import com.leyuz.uc.auth.role.RoleApplication;
import com.leyuz.uc.auth.role.RoleE;
import com.leyuz.uc.auth.role.UserRoleMapper;
import com.leyuz.uc.auth.role.UserRolePO;
import com.leyuz.uc.auth.role.dto.*;
import com.leyuz.uc.auth.role.gateway.RoleGateway;
import com.leyuz.uc.user.UserApplication;
import com.leyuz.uc.user.UserE;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserRoleApplication {
    private final UserRoleMapper userRoleMapper;
    private final RoleGateway roleGateway;
    private final UserApplication userApplication;
    private final RoleApplication roleApplication;
    private static final String GLOBAL_ROLE_SCOPE = "ALL";

    public List<UserRoleDTO> listByUserId(Long userId) {
        List<UserRolePO> userRolePOList = userRoleMapper.listValidRolesByUserId(userId);
        return userRolePOList.stream()
                .map(this::convertToDTO)
                .toList();
    }

    public boolean assign(UserRoleCreateCmd userRoleCreateCmd) {
        if (userRoleCreateCmd.getExpireTime() != null
                && userRoleCreateCmd.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new ValidationException("过期时间不能早于当前时间");
        }

        UserE userE = userApplication.getByIdFromCache(userRoleCreateCmd.getUserId());
        if (userE.getUserId() == null) {
            throw new ValidationException("用户不存在或已注销无法完成授权操作");
        }

        roleApplication.listRoles().stream()
                .filter(roleE -> roleE.getRoleKey().equals(userRoleCreateCmd.getRoleKey()))
                .findFirst()
                .orElseThrow(() -> new ValidationException("角色不存在请重新选择已存在的角色"));

        UserRolePO userRolePO = new UserRolePO();
        BeanUtils.copyProperties(userRoleCreateCmd, userRolePO);
        if (StringUtils.isEmpty(userRoleCreateCmd.getRoleScope())) {
            userRolePO.setRoleScope(GLOBAL_ROLE_SCOPE);
        }
        BaseEntityUtils.setCreateBaseEntity(userRolePO);
        return userRoleMapper.insert(userRolePO) > 0;
    }

    public boolean cancel(Long id) {
        return userRoleMapper.removeById(id) > 0;
    }

    public boolean cancel(Long userId, String roleScope, String roleKey) {
        // 获取用户的所有有效角色
        List<UserRolePO> userRoles = userRoleMapper.listValidRolesByUserId(userId).stream()
                .filter(ur -> isRoleValid(ur, roleScope))
                .filter(ur -> ur.getRoleKey().equals(roleKey))
                .toList();
        if (userRoles.isEmpty()) {
            throw new ValidationException("用户未授权该角色或当前角色已失效");
        }
        return userRoleMapper.cancel(userId, roleScope, roleKey) > 0;
    }

    public List<UserRoleDTO> listByRoleKey(String roleKey) {
        List<UserRolePO> userRoles = userRoleMapper.listByRoleKey(roleKey);
        return userRoles.stream()
                .map(this::convertToDTO)
                .toList();
    }

    private UserRoleDTO convertToDTO(UserRolePO userRole) {
        if (userRole == null) {
            return null;
        }
        return UserRoleDTO.builder()
                .id(userRole.getId())
                .userId(userRole.getUserId())
                .roleKey(userRole.getRoleKey())
                .roleScope(userRole.getRoleScope())
                .createBy(userRole.getCreateBy())
                .createTime(userRole.getCreateTime())
                .expireTime(userRole.getExpireTime())
                .build();
    }

    public List<String> getHighestPriorityRoles(Long userId, String roleScope, String defaultRoleKey) {
        if (userId == null || userId <= 0) {
            if (StringUtils.isEmpty(defaultRoleKey)) {
                return Collections.emptyList();
            } else {
                return List.of(defaultRoleKey);
            }
        }

        // 获取用户的所有有效角色
        List<UserRolePO> userRoles = userRoleMapper.listValidRolesByUserId(userId).stream()
                .filter(ur -> isRoleValid(ur, roleScope))
                .toList();

        if (userRoles.isEmpty()) {
            if (StringUtils.isEmpty(defaultRoleKey)) {
                return Collections.emptyList();
            } else {
                return List.of(defaultRoleKey);
            }
        }

        // 获取角色详细信息
        List<String> roleKeys = userRoles.stream()
                .map(UserRolePO::getRoleKey)
                .toList();

        // 获取所有角色并按优先级分组
        List<RoleE> allRoles = roleGateway.list();
        List<RoleE> roles = allRoles.stream()
                .filter(role -> roleKeys.contains(role.getRoleKey()))
                .toList();

        // 按优先级分组
        Map<Integer, List<RoleE>> priorityRoles = roles.stream()
                .collect(Collectors.groupingBy(RoleE::getPriority));

        // 获取最高优先级
        Optional<Integer> maxPriority = priorityRoles.keySet().stream().max(Integer::compareTo);

        // 获取最高优先级的角色key
        return maxPriority.map(priority -> priorityRoles.getOrDefault(priority, Collections.emptyList()).stream()
                        .map(RoleE::getRoleKey)
                        .toList())
                .orElse(Collections.emptyList());
    }

    private boolean isRoleValid(UserRolePO userRolePO, String roleScope) {
        // roleScope 为空表示忽略范围
        if (StringUtils.isEmpty(roleScope)) {
            return true;
        }
        if (GLOBAL_ROLE_SCOPE.equalsIgnoreCase(userRolePO.getRoleScope())) {
            // 默认不过滤全局范围的角色
            return true;
        }
        return userRolePO.getRoleScope().equalsIgnoreCase(roleScope);
    }

    public CustomPage<UserRoleVO> pageUserRoles(UserRolePageQuery query) {
        PageQuery pageQuery = PageQuery.builder()
                .pageNo(query.getPageNo())
                .pageSize(query.getPageSize())
                .build();

        Map<String, Object> params = pageQuery.getParams();
        params.put("roleKey", query.getRoleKey());
        params.put("userName", query.getUserName());
        params.put("roleScope", query.getRoleScope());
        params.put("userId", query.getUserId());

        Page<UserRolePO> page = new Page<>(pageQuery.getPageNo(), pageQuery.getPageSize());
        Page<UserRolePO> userRolePage = userRoleMapper.queryUserRoles(page, params);
        return DataBaseUtils.createCustomPage(userRolePage, this::convertToVO);
    }

    private UserRoleVO convertToVO(UserRolePO userRolePO) {
        if (userRolePO == null) {
            return null;
        }
        String userName = userApplication.getByIdFromCache(userRolePO.getUserId()).getUserName();
        String createUserName = userApplication.getByIdFromCache(userRolePO.getCreateBy()).getUserName();
        Map<String, String> roleKeyNameMap = new HashMap<>();
        for (RoleDTO role : roleApplication.listRoles()) {
            roleKeyNameMap.put(role.getRoleKey(), role.getRoleName());
        }
        return UserRoleVO.builder()
                .id(userRolePO.getId())
                .userId(userRolePO.getUserId())
                .userName(userName)
                .roleKey(userRolePO.getRoleKey())
                .roleName(roleKeyNameMap.get(userRolePO.getRoleKey()))
                .roleScope(userRolePO.getRoleScope())
                .createUserName(createUserName)
                .createTime(userRolePO.getCreateTime())
                .expireTime(userRolePO.getExpireTime())
                .build();
    }
}
