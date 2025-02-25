package com.leyuz.uc.auth.role.mybatis;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.leyuz.common.mybatis.PageQuery;
import com.leyuz.uc.auth.role.UserRolePO;

import java.util.List;

public interface IUserRoleService extends IService<UserRolePO> {
    List<UserRolePO> listByUserId(Long userId);

    /**
     * 获取用户有效的角色列表(未过期)
     */
    List<UserRolePO> listValidRolesByUserId(Long userId);

    boolean removeById(Long id);

    List<UserRolePO> listByRoleKey(String roleKey);

    /**
     * 分页查询用户角色列表
     *
     * @param pageQuery 分页查询参数
     * @return 分页结果
     */
    Page<UserRolePO> queryUserRoles(PageQuery pageQuery);

    boolean cancel(Long userId, String roleScope, String roleKey);
}