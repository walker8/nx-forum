package com.leyuz.uc.auth.role;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<RolePO> {

    @Select("SELECT r.* FROM uc_roles r " +
            "LEFT JOIN uc_user_roles ur ON ur.role_key = r.role_key " +
            "WHERE ur.user_id = #{userId} AND r.is_deleted = 0")
    List<RolePO> selectListByUserId(@Param("userId") Long userId);

    /**
     * 分页查询角色
     *
     * @param page   分页参数
     * @param params 查询参数
     * @return 分页结果
     */
    Page<RolePO> queryRoles(Page<RolePO> page, @Param("params") java.util.Map<String, Object> params);
} 