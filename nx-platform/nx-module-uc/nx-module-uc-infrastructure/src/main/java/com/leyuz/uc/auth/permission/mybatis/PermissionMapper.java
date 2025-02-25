package com.leyuz.uc.auth.permission.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyuz.uc.auth.permission.PermissionPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<PermissionPO> {

    @Select("SELECT * from uc_permissions WHERE perm_id IN " +
            "(SELECT DISTINCT perm_id FROM uc_role_permissions " +
            "WHERE role_key = #{roleKey} AND is_deleted=0) AND is_deleted=0")
    List<PermissionPO> selectListByRoleKey(@Param("roleKey") String roleKey);

    @Select("SELECT perms from uc_permissions WHERE perm_id IN " +
            "(SELECT DISTINCT perm_id FROM uc_role_permissions " +
            "WHERE role_key = #{roleKey} AND is_deleted=0) AND is_deleted=0")
    List<String> selectPermsByRoleKey(@Param("roleKey") String roleKey);
} 