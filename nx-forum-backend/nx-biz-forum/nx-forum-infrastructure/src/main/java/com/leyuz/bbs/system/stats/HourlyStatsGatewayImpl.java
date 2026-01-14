package com.leyuz.bbs.system.stats;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 每小时统计网关实现
 *
 * @author Walker
 * @since 2025-01-11
 */
@Repository
@RequiredArgsConstructor
public class HourlyStatsGatewayImpl implements HourlyStatsGateway {

    private final HourlyStatsMapper hourlyStatsMapper;

    @Override
    public void saveOrUpdate(HourlyStatsPO stats) {
        HourlyStatsPO existing = getByDateAndHour(
            stats.getStatDate(),
            stats.getStatHour(),
            stats.getTerminalType()
        );

        if (existing == null) {
            hourlyStatsMapper.insert(stats);
        } else {
            LambdaUpdateWrapper<HourlyStatsPO> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(HourlyStatsPO::getStatId, existing.getStatId())
                .set(HourlyStatsPO::getUniqueIpCount, stats.getUniqueIpCount())
                .set(HourlyStatsPO::getGuestIpCount, stats.getGuestIpCount())
                .set(HourlyStatsPO::getUserIpCount, stats.getUserIpCount());

            hourlyStatsMapper.update(null, wrapper);
        }
    }

    @Override
    public HourlyStatsPO getByDateAndHour(LocalDate date, int hour, String terminalType) {
        return hourlyStatsMapper.selectOne(
            new LambdaQueryWrapper<HourlyStatsPO>()
                .eq(HourlyStatsPO::getStatDate, date)
                .eq(HourlyStatsPO::getStatHour, hour)
                .eq(HourlyStatsPO::getTerminalType, terminalType)
        );
    }

    @Override
    public List<HourlyStatsPO> getStatsByDate(LocalDate date) {
        return hourlyStatsMapper.selectList(
            new LambdaQueryWrapper<HourlyStatsPO>()
                .eq(HourlyStatsPO::getStatDate, date)
                .eq(HourlyStatsPO::getTerminalType, "ALL")
                .orderByAsc(HourlyStatsPO::getStatHour)
        );
    }

    @Override
    public int deleteOldStats(int days) {
        LocalDate cutoffDate = LocalDate.now().minusDays(days);
        return hourlyStatsMapper.delete(
            new LambdaQueryWrapper<HourlyStatsPO>()
                .lt(HourlyStatsPO::getStatDate, cutoffDate)
        );
    }
}
