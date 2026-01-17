package com.leyuz.bbs.system.access;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 访问日志网关实现
 *
 * @author Walker
 * @since 2025-01-11
 */
@Repository
@RequiredArgsConstructor
public class AccessLogGatewayImpl implements AccessLogGateway {

    private final AccessLogMapper accessLogMapper;

    @Override
    public void save(AccessLogPO log) {
        accessLogMapper.insert(log);
    }

    @Override
    public long countUniqueIps(LocalDateTime startTime, LocalDateTime endTime,
                             String terminalType, String platform) {
        LambdaQueryWrapper<AccessLogPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(AccessLogPO::getAccessTime, startTime, endTime);

        if (!"ALL".equals(terminalType)) {
            wrapper.eq(AccessLogPO::getTerminalType, terminalType);
        }
        if (!"ALL".equals(platform)) {
            wrapper.eq(AccessLogPO::getPlatform, platform);
        }

        List<AccessLogPO> logs = accessLogMapper.selectList(wrapper);
        return logs.stream()
            .map(AccessLogPO::getIpAddress)
            .distinct()
            .count();
    }

    @Override
    public long countGuestIps(LocalDateTime startTime, LocalDateTime endTime,
                             String terminalType, String platform) {
        LambdaQueryWrapper<AccessLogPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(AccessLogPO::getAccessTime, startTime, endTime)
            .eq(AccessLogPO::getUserId, 0L);

        if (!"ALL".equals(terminalType)) {
            wrapper.eq(AccessLogPO::getTerminalType, terminalType);
        }
        if (!"ALL".equals(platform)) {
            wrapper.eq(AccessLogPO::getPlatform, platform);
        }

        List<AccessLogPO> logs = accessLogMapper.selectList(wrapper);
        return logs.stream()
            .map(AccessLogPO::getIpAddress)
            .distinct()
            .count();
    }

    @Override
    public long countUserIps(LocalDateTime startTime, LocalDateTime endTime,
                            String terminalType, String platform) {
        LambdaQueryWrapper<AccessLogPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(AccessLogPO::getAccessTime, startTime, endTime)
            .ne(AccessLogPO::getUserId, 0L);

        if (!"ALL".equals(terminalType)) {
            wrapper.eq(AccessLogPO::getTerminalType, terminalType);
        }
        if (!"ALL".equals(platform)) {
            wrapper.eq(AccessLogPO::getPlatform, platform);
        }

        List<AccessLogPO> logs = accessLogMapper.selectList(wrapper);
        return logs.stream()
            .map(AccessLogPO::getIpAddress)
            .distinct()
            .count();
    }

    @Override
    public int deleteOldLogs(int days) {
        LocalDateTime cutoffDateTime = LocalDateTime.now().minusDays(days);
        return accessLogMapper.delete(
                new LambdaQueryWrapper<AccessLogPO>()
                        .lt(AccessLogPO::getAccessTime, cutoffDateTime)
        );
    }
}
