package com.leyuz.bbs.system.access;

import com.leyuz.common.dto.UserClientInfo;
import com.leyuz.common.utils.HeaderUtils;
import com.leyuz.common.utils.UserAgentUtils;
import com.leyuz.module.cache.GenericCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
     */
    @Async("accessLogExecutor")
    public void logAccessAsync() {
        try {
            // 获取请求头信息（从ThreadLocal中获取，不使用request对象）
            String ip = HeaderUtils.getIp();
            String deviceId = HeaderUtils.getDeviceId();
            Long userId = HeaderUtils.getUserId();
            String userAgent = HeaderUtils.getUserAgent();
            String appVersion = HeaderUtils.getAppVersion();

            // 构建缓存Key: access_log:{ip}:{deviceId}
            String cacheKey = ip + ":" + (deviceId != null ? deviceId : "");

            // 检查缓存（20分钟去重）
            String cached = accessLogCache.get(cacheKey);
            if (cached != null) {
                log.debug("访问日志已存在（缓存），跳过记录：ip={}, deviceId={}", ip, deviceId);
                return;
            }

            // 设置缓存标记（20分钟TTL）
            accessLogCache.put(cacheKey, "1", 1200L);

            // 解析客户端信息
            UserClientInfo clientInfo = UserAgentUtils.getClientInfo(userAgent);

            // 判断终端类型
            String terminalType = determineTerminalType(deviceId, userAgent);

            // 构建访问日志PO
            AccessLogPO accessLog = AccessLogPO.builder()
                    .accessTime(LocalDateTime.now())
                    .ipAddress(ip)
                    .deviceId(deviceId != null ? deviceId : "")
                    .appVersion(appVersion)
                    .userId(userId)
                    .userAgent(userAgent)
                    .terminalType(terminalType)
                    .platform(clientInfo.getPlatform())
                    .browserName(clientInfo.getBrowser())
                    .build();

            // 保存到数据库
            accessLogGateway.save(accessLog);
            log.debug("访问日志记录成功：ip={}, deviceId={}, terminalType={}", ip, deviceId, terminalType);

        } catch (Exception e) {
            log.error("记录访问日志失败", e);
        }
    }

    /**
     * 判断终端类型
     *
     * @param deviceId  设备ID
     * @param userAgent User-Agent
     * @return 终端类型（PC/MOBILE/APP）
     */
    private String determineTerminalType(String deviceId, String userAgent) {
        // 如果有deviceId，认为是App端
        if (deviceId != null && !deviceId.isEmpty()) {
            return "APP";
        }

        // 根据User-Agent判断
        String ua = userAgent.toLowerCase();
        if (ua.contains("android") || ua.contains("iphone") || ua.contains("ipad") ||
                ua.contains("mobile") || ua.contains("touch")) {
            return "MOBILE";
        }

        return "PC";
    }
}
