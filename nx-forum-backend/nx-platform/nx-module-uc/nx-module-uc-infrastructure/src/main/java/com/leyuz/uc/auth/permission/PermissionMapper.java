package com.leyuz.uc.auth.permission;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<PermissionPO> {

    /**
     * 根据角色标识查询权限列表
     *
     * @param roleKey 角色标识
     * @return 权限列表
     */
    @Select("SELECT * FROM uc_permissions WHERE perms IN " +
            "(SELECT perms FROM uc_role_permissions " +
            "WHERE role_key = #{roleKey} AND is_deleted = 0) AND is_deleted = 0")
    List<PermissionPO> selectListByRoleKey(@Param("roleKey") String roleKey);

    /**
     * 根据权限标识查询权限
     *
     * @param perms 权限标识
     * @return 权限对象
     */
    @Select("SELECT * FROM uc_permissions " +
            "WHERE perms = #{perms} AND is_deleted = 0 LIMIT 1")
    PermissionPO getByPerms(@Param("perms") String perms);
}