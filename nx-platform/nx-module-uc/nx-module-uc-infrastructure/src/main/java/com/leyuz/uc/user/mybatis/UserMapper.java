package com.leyuz.uc.user.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyuz.uc.user.UserPO;
import org.apache.ibatis.annotations.Mapper;

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

}
