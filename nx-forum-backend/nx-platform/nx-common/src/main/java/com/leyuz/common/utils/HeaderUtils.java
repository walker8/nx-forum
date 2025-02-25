package com.leyuz.common.utils;

import com.leyuz.common.context.RequestHeader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * 请求头工具类
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HeaderUtils {
    // TransmittableThreadLocal
    private static final ThreadLocal<RequestHeader> HTTP_REQUEST_CONTEXT = new InheritableThreadLocal<>();

    public static void setHeader(RequestHeader requestHeader) {
        if (requestHeader != null) {
            remove();
        }
        HTTP_REQUEST_CONTEXT.set(requestHeader);
    }

    /**
     * 重新设置用户信息
     *
     * @param userId
     * @param accessToken
     */
    public static void setUser(Long userId, String accessToken) {
        RequestHeader requestHeader = HTTP_REQUEST_CONTEXT.get();
        if (requestHeader != null) {
            requestHeader.setUserId(userId);
            requestHeader.setAccessToken(accessToken);
        }
    }

    public static Long getUserId() {
        RequestHeader requestHeader = HTTP_REQUEST_CONTEXT.get();
        if (requestHeader != null) {
            return requestHeader.getUserId();
        }
        return 0L;
    }

    public static String getAccessToken() {
        RequestHeader requestHeader = HTTP_REQUEST_CONTEXT.get();
        if (requestHeader != null) {
            return requestHeader.getAccessToken();
        }
        return "";
    }

    public static String getToken() {
        RequestHeader requestHeader = HTTP_REQUEST_CONTEXT.get();
        if (requestHeader != null) {
            return requestHeader.getToken();
        }
        return "";
    }

    public static String getIp() {
        RequestHeader requestHeader = HTTP_REQUEST_CONTEXT.get();
        if (requestHeader != null) {
            return requestHeader.getIp();
        }
        return "";
    }

    public static String getUserAgent() {
        RequestHeader requestHeader = HTTP_REQUEST_CONTEXT.get();
        if (requestHeader != null) {
            return requestHeader.getUserAgent();
        }
        return "";
    }

    public static String getReferer() {
        RequestHeader requestHeader = HTTP_REQUEST_CONTEXT.get();
        if (requestHeader != null) {
            return requestHeader.getReferer();
        }
        return "";
    }

    /**
     * 获取域名（带schema）
     *
     * @return
     */
    public static String getDomain() {
        RequestHeader requestHeader = HTTP_REQUEST_CONTEXT.get();
        if (requestHeader != null) {
            return requestHeader.getDomain();
        }
        return "";
    }

    /**
     * 获取域名（不带schema）
     *
     * @return
     */
    public static String getHost() {
        String domain = getDomain();
        if (StringUtils.isEmpty(domain)) {
            return "";
        }
        return domain.split("/")[2];
    }

    public static String getDeviceId() {
        RequestHeader requestHeader = HTTP_REQUEST_CONTEXT.get();
        if (requestHeader != null) {
            String deviceId = requestHeader.getDeviceId();
            return null == deviceId ? "" : deviceId;
        }
        return "";
    }

    public static void remove() {
        HTTP_REQUEST_CONTEXT.remove();
    }
}
