package com.leyuz.uc.auth.permission;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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

    /**
     * 根据权限标识查询权限
     *
     * @param perms 权限标识
     * @return 权限对象
     */
    @Select("SELECT * FROM uc_permissions " +
            "WHERE perms = #{perms} AND is_deleted = 0 LIMIT 1")
    PermissionPO getByPerms(@Param("perms") String perms);

    /**
     * 根据权限ID列表查询权限标识列表
     *
     * @param permIds 权限ID列表
     * @return 权限标识列表
     */
    @Select("<script>" +
            "SELECT perms FROM uc_permissions " +
            "WHERE perm_id IN " +
            "<foreach item='id' collection='permIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND is_deleted = 0" +
            "</script>")
    List<String> listPermsByIds(@Param("permIds") List<Long> permIds);
} 