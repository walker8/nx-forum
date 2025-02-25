package com.leyuz.uc.auth.role.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyuz.uc.auth.role.RolePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<RolePO> {

    @Select("SELECT r.* FROM bbs_roles r " +
            "LEFT JOIN bbs_user_roles ur ON ur.role_key = r.role_key " +
            "WHERE ur.user_id = #{userId} AND r.is_deleted = 0")
    List<RolePO> selectListByUserId(@Param("userId") Long userId);
} 