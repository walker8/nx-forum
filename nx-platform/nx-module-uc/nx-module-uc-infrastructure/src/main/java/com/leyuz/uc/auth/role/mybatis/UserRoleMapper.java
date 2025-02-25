package com.leyuz.uc.auth.role.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyuz.uc.auth.role.UserRolePO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRolePO> {
} 