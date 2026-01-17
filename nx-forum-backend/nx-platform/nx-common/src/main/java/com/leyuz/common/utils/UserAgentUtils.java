package com.leyuz.common.utils;

import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.leyuz.common.dto.UserClientInfo;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 客户端信息工具类
 *
 * @author Walker
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserAgentUtils {
    private static final String UNKNOWN = "unknown";
    private static final String TERMINAL_PC = "PC";
    private static final String TERMINAL_MOBILE = "MOBILE";
    private static final String TERMINAL_APP = "APP";
    private static final String APP_UA_PREFIX = "NXForumApp/";

    /**
     * 获取客户端信息（含终端类型）
     *
     * @param userAgent  User-Agent字符串
     * @param appVersion App版本（可选，用于APP检测）
     * @return 客户端信息
     */
    public static UserClientInfo getClientInfo(String userAgent, String appVersion) {
        UserClientInfo userClientInfo = new UserClientInfo();

        // 检测终端类型
        String terminalType = determineTerminalType(userAgent, appVersion);
        userClientInfo.setTerminalType(terminalType);

        // 原有平台检测逻辑
        UserAgent agent = UserAgentUtil.parse(userAgent);
        if (agent == null) {
            userClientInfo.setPlatform(UNKNOWN);
            return userClientInfo;
        }
        if (!UNKNOWN.equalsIgnoreCase(agent.getPlatform().getName())) {
            userClientInfo.setPlatform(agent.getPlatform().getName());
            userClientInfo.setOs(agent.getOs().getName() + " " + agent.getOsVersion());
        } else if (userAgent.toLowerCase().contains("ios")) {
            userClientInfo.setPlatform("iPhone");
            userClientInfo.setOs("iOS");
        } else {
            userClientInfo.setPlatform(agent.getPlatform().getName());
        }
        if (!UNKNOWN.equalsIgnoreCase(agent.getBrowser().getName())) {
            userClientInfo.setBrowser(agent.getBrowser().getName());
        }
        return userClientInfo;
    }

    /**
     * 保持向后兼容 - 不带appVersion的版本
     */
    public static UserClientInfo getClientInfo(String userAgent) {
        return getClientInfo(userAgent, null);
    }

    /**
     * 检测终端类型（整合后的统一方法）
     *
     * @param userAgent  User-Agent字符串
     * @param appVersion App版本（可选，来自X-App-Version header）
     * @return 终端类型（PC/MOBILE/APP）
     */
    private static String determineTerminalType(String userAgent, String appVersion) {
        if (userAgent == null || userAgent.isEmpty()) {
            return TERMINAL_PC;
        }

        String ua = userAgent.toLowerCase();

        // 1. 优先：通过X-App-Version header检测App
        if (appVersion != null && !appVersion.isEmpty()) {
            return TERMINAL_APP;
        }

        // 2. 次优：通过User-Agent中的NXForumApp/前缀检测App
        if (ua.contains(APP_UA_PREFIX.toLowerCase())) {
            return TERMINAL_APP;
        }

        // 3. 移动端检测
        if (ua.contains("android") || ua.contains("iphone") || ua.contains("ipad") ||
                ua.contains("mobile") || ua.contains("touch")) {
            return TERMINAL_MOBILE;
        }

        // 4. 默认PC
        return TERMINAL_PC;
    }
}
