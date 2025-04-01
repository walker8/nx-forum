package com.leyuz.uc.log.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyuz.uc.log.UserLogPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户日志Mapper接口
 */
@Mapper
public interface UserLogMapper extends BaseMapper<UserLogPO> {
} 