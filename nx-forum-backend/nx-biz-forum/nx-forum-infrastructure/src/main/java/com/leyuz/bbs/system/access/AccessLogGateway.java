package com.leyuz.bbs.system.access;

import java.time.LocalDateTime;

/**
 * 访问日志网关接口
 *
 * @author Walker
 * @since 2025-01-11
 */
public interface AccessLogGateway {

    /**
     * 保存访问日志
     *
     * @param log 访问日志实体
     */
    void save(AccessLogPO log);

    /**
     * 统计独立IP数
     *
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @param terminalType  终端类型
     * @param platform      平台
     * @return 独立IP数
     */
    long countUniqueIps(LocalDateTime startTime, LocalDateTime endTime,
                       String terminalType, String platform);

    /**
     * 统计游客独立IP数
     *
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @param terminalType  终端类型
     * @param platform      平台
     * @return 游客独立IP数
     */
    long countGuestIps(LocalDateTime startTime, LocalDateTime endTime,
                      String terminalType, String platform);

    /**
     * 统计登录用户独立IP数
     *
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @param terminalType  终端类型
     * @param platform      平台
     * @return 登录用户独立IP数
     */
    long countUserIps(LocalDateTime startTime, LocalDateTime endTime,
                     String terminalType, String platform);

    /**
     * 删除指定天数之前的访问日志
     *
     * @param days 天数
     * @return 删除的记录数
     */
    int deleteOldLogs(int days);
}
