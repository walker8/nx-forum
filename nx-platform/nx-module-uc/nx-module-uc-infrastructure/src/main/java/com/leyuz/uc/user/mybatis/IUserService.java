package com.leyuz.uc.user.mybatis;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.leyuz.common.mybatis.PageQuery;
import com.leyuz.uc.user.UserPO;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author walker
 * @since 2024-01-06
 */
public interface IUserService extends IService<UserPO> {

    Page<UserPO> queryUsers(PageQuery pageQuery);

    /**
     * 根据名称模糊查询用户
     *
     * @param name
     * @param num
     * @return
     */
    List<UserPO> queryUsersByName(String name, int num);
}
