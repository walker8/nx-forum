package com.leyuz.bbs.system.access;

import com.leyuz.common.dto.UserClientInfo;
import com.leyuz.common.utils.UserAgentUtils;
import com.leyuz.module.cache.GenericCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 访问日志应用服务
 *
 * @author Walker
 * @since 2025-01-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccessLogApplication {

    private final AccessLogGateway accessLogGateway;
    private final GenericCache<String, String> accessLogCache;

    /**
     * 异步记录访问日志（包含20分钟去重检查）
     *
     * @param ip         IP地址
     * @param deviceId   设备ID
     * @param userId     用户ID
     * @param userAgent  User-Agent
     * @param appVersion App版本
     */
    @Async("accessLogExecutor")
    public void logAccessAsync(String ip, String deviceId, Long userId,
                               String userAgent, String appVersion) {
        try {
            if (StringUtils.isEmpty(deviceId)) {
                // deviceId 为空说明是服务端访问，不统计
                return;
            }
            // 构建缓存Key: access_log:{ip}:{deviceId}
            String cacheKey = ip + ":" + deviceId;

            // 检查缓存（20分钟去重）
            String cached = accessLogCache.get(cacheKey);
            if (cached != null) {
                log.debug("访问日志已存在（缓存），跳过记录：ip={}, deviceId={}", ip, deviceId);
                return;
            }

            // 设置缓存标记（20分钟TTL）
            accessLogCache.put(cacheKey, "1", 1200L);

            // 简化：直接获取完整客户端信息（包含终端类型）
            UserClientInfo clientInfo = UserAgentUtils.getClientInfo(userAgent, appVersion);

            // 构建访问日志PO
            AccessLogPO accessLog = AccessLogPO.builder()
                    .accessTime(LocalDateTime.now())
                    .ipAddress(ip)
                    .deviceId(deviceId)
                    .appVersion(appVersion)
                    .userId(userId)
                    .userAgent(userAgent)
                    .terminalType(clientInfo.getTerminalType())
                    .platform(clientInfo.getPlatform())
                    .browserName(clientInfo.getBrowser())
                    .build();

            // 保存到数据库
            accessLogGateway.save(accessLog);
            log.debug("访问日志记录成功：ip={}, deviceId={}, terminalType={}", ip, deviceId, clientInfo.getTerminalType());

        } catch (Exception e) {
            log.error("记录访问日志失败", e);
        }
    }
}
