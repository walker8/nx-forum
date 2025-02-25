package com.leyuz.uc.auth.role.mybatis;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.leyuz.common.mybatis.PageQuery;
import com.leyuz.uc.auth.role.RolePO;

import java.util.List;

public interface IRoleService extends IService<RolePO> {
    List<RolePO> listByUserId(Long userId);

    Page<RolePO> queryRoles(PageQuery pageQuery);
}