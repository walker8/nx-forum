package com.leyuz.bbs.system.stats;

import java.time.LocalDate;
import java.util.List;

/**
 * 每小时统计网关接口
 *
 * @author Walker
 * @since 2025-01-11
 */
public interface HourlyStatsGateway {

    /**
     * 保存或更新每小时统计
     *
     * @param stats 统计实体
     */
    void saveOrUpdate(HourlyStatsPO stats);

    /**
     * 根据日期和小时获取统计
     *
     * @param date          日期
     * @param hour          小时
     * @param terminalType  终端类型
     * @return 统计实体
     */
    HourlyStatsPO getByDateAndHour(LocalDate date, int hour, String terminalType);

    /**
     * 查询指定日期的所有小时统计（终端类型为ALL）
     *
     * @param date 日期
     * @return 该日期的所有小时统计列表
     */
    List<HourlyStatsPO> getStatsByDate(LocalDate date);

    /**
     * 清理指定天数之前的统计数据
     *
     * @param days 天数
     * @return 删除的记录数
     */
    int deleteOldStats(int days);
}
