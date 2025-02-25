package com.leyuz.uc.log;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 用户日志Mapper接口
 */
@Mapper
public interface UserLogMapper extends BaseMapper<UserLogPO> {

    /**
     * 分页查询用户日志
     *
     * @param page   分页参数
     * @param params 查询参数
     * @return 分页结果
     */
    Page<UserLogPO> queryUserLogs(Page<UserLogPO> page, @Param("params") Map<String, Object> params);
} 