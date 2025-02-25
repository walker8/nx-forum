package com.leyuz.uc.auth.role;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermissionPO> {

    /**
     * 根据角色标识查询角色权限列表
     *
     * @param roleKey 角色标识
     * @return 角色权限列表
     */
    @Select("SELECT * FROM uc_role_permissions " +
            "WHERE role_key = #{roleKey} AND is_deleted = 0")
    List<RolePermissionPO> listByRoleKey(@Param("roleKey") String roleKey);

    /**
     * 逻辑删除角色的所有权限
     *
     * @param roleKey 角色标识
     * @return 影响行数
     */
    @Update("UPDATE uc_role_permissions SET is_deleted = 1, update_time = NOW() " +
            "WHERE role_key = #{roleKey} AND is_deleted = 0")
    int removeByRoleKey(@Param("roleKey") String roleKey);
} 