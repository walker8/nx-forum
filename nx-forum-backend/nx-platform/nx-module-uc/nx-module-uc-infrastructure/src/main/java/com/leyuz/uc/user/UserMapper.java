package com.leyuz.uc.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author walker
 * @since 2024-01-06
 */
@Mapper
public interface UserMapper extends BaseMapper<UserPO> {

    /**
     * 分页查询用户
     *
     * @param page    分页参数
     * @param params  查询参数
     * @param orderBy 排序字段
     * @param isAsc   是否升序
     * @return 分页结果
     */
    Page<UserPO> queryUsers(Page<UserPO> page, @Param("params") Map<String, Object> params,
                            @Param("orderBy") String orderBy, @Param("isAsc") Boolean isAsc);

    /**
     * 根据名称模糊查询用户
     *
     * @param name 用户名
     * @param num  数量限制
     * @return 用户列表
     */
    @Select("<script>" +
            "SELECT * FROM uc_users " +
            "WHERE is_deleted = 0 " +
            "<if test='name != null and name != \"\"'> " +
            "AND user_name LIKE CONCAT('%', #{name}, '%') " +
            "</if>" +
            "LIMIT #{num}" +
            "</script>")
    List<UserPO> queryUsersByName(@Param("name") String name, @Param("num") int num);
}
