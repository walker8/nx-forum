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

    /**
     * 获取客户端信息
     *
     * @param userAgent
     * @return
     */
    public static UserClientInfo getClientInfo(String userAgent) {
        UserClientInfo userClientInfo = new UserClientInfo();
        UserAgent agent = UserAgentUtil.parse(userAgent);
        if (agent == null) {
            return userClientInfo;
        }
        if (!UNKNOWN.equalsIgnoreCase(agent.getPlatform().getName())) {
            userClientInfo.setOs(agent.getPlatform().getName() + " " + agent.getOsVersion());
        }
        if (!UNKNOWN.equalsIgnoreCase(agent.getBrowser().getName())) {
            userClientInfo.setBrowser(agent.getBrowser().getName());
        }
        return userClientInfo;
    }
}
