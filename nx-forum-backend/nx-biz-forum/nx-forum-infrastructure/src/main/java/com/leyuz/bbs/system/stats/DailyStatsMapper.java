package com.leyuz.bbs.system.stats;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 每日统计Mapper接口
 *
 * @author Walker
 * @since 2025-01-11
 */
@Mapper
public interface DailyStatsMapper extends BaseMapper<DailyStatsPO> {
}
