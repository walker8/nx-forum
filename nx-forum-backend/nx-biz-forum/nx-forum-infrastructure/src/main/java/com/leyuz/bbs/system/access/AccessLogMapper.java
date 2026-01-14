package com.leyuz.bbs.system.access;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 访问日志Mapper接口
 *
 * @author Walker
 * @since 2025-01-11
 */
@Mapper
public interface AccessLogMapper extends BaseMapper<AccessLogPO> {
}
