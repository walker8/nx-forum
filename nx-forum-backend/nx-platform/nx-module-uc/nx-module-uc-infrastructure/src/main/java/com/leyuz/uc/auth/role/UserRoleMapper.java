package com.leyuz.uc.auth.role;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRolePO> {

    /**
     * 根据用户ID查询用户角色列表
     *
     * @param userId 用户ID
     * @return 用户角色列表
     */
    @Select("SELECT * FROM uc_user_roles WHERE user_id = #{userId} AND is_deleted = 0")
    List<UserRolePO> listByUserId(@Param("userId") Long userId);

    /**
     * 获取用户有效的角色列表(未过期)
     *
     * @param userId 用户ID
     * @return 用户角色列表
     */
    @Select("SELECT * FROM uc_user_roles " +
            "WHERE user_id = #{userId} AND is_deleted = 0 " +
            "AND (expire_time IS NULL OR expire_time > NOW())")
    List<UserRolePO> listValidRolesByUserId(@Param("userId") Long userId);

    /**
     * 逻辑删除用户角色
     *
     * @param id 用户角色ID
     * @return 影响行数
     */
    @Update("UPDATE uc_user_roles SET is_deleted = 1, update_time = NOW() WHERE id = #{id}")
    int removeById(@Param("id") Long id);

    /**
     * 根据角色标识查询用户角色列表
     *
     * @param roleKey 角色标识
     * @return 用户角色列表
     */
    @Select("SELECT * FROM uc_user_roles " +
            "WHERE role_key = #{roleKey} AND is_deleted = 0 " +
            "ORDER BY create_time DESC")
    List<UserRolePO> listByRoleKey(@Param("roleKey") String roleKey);

    /**
     * 分页查询用户角色列表
     *
     * @param page   分页参数
     * @param params 查询参数
     * @return 分页结果
     */
    Page<UserRolePO> queryUserRoles(Page<UserRolePO> page, @Param("params") java.util.Map<String, Object> params);

    /**
     * 取消用户角色
     *
     * @param userId    用户ID
     * @param roleScope 角色范围
     * @param roleKey   角色标识
     * @return 影响行数
     */
    @Update("<script>" +
            "UPDATE uc_user_roles SET is_deleted = 1, update_time = NOW() " +
            "WHERE user_id = #{userId} AND is_deleted = 0 " +
            "<if test='roleKey != null and roleKey != \"\"'> " +
            "AND role_key = #{roleKey} " +
            "</if>" +
            "<if test='roleScope != null and roleScope != \"\"'> " +
            "AND role_scope = #{roleScope} " +
            "</if>" +
            "<if test='roleScope == null or roleScope == \"\"'> " +
            "AND role_scope = '' " +
            "</if>" +
            "</script>")
    int cancel(@Param("userId") Long userId, @Param("roleScope") String roleScope, @Param("roleKey") String roleKey);
} 