package com.leyuz.uc.auth.filter;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import com.leyuz.common.context.RequestHeader;
import com.leyuz.common.utils.HeaderUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Optional;

/**
 * 头信息 过滤器
 *
 * @author Walker
 * @since 2024/4/4
 */
@Slf4j
public class RequestHeaderFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // DEBUG: Log all headers received
        if (log.isDebugEnabled()) {
            log.debug("=== Request Headers Debug ===");
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                // Truncate long values for readability
                String displayValue = headerValue != null && headerValue.length() > 50
                        ? headerValue.substring(0, 50) + "..."
                        : headerValue;
                log.debug("Header: {} = {}", headerName, displayValue);
            }
        }

        RequestHeader requestHeader = new RequestHeader();
        requestHeader.setIp(getClientIP(request));
        requestHeader.setUserId(0L);
        requestHeader.setUserAgent(CharSequenceUtil.sub(request.getHeader("User-Agent"), 0, 255));
        requestHeader.setReferer(request.getHeader("referer"));
        // Extract deviceId with debug logging and null safety
        String deviceId = request.getHeader("X-Device-Id");
        requestHeader.setDeviceId(deviceId != null ? deviceId : "");
        requestHeader.setToken(Optional.ofNullable(getCookieValue(request, "x_token")).orElse(request.getHeader("X-Token")));
        requestHeader.setAccessToken(Optional.ofNullable(getCookieValue(request, "accessToken")).orElse(request.getHeader(HttpHeaders.AUTHORIZATION)));
        requestHeader.setDomain(getDomain(request));
        requestHeader.setAppVersion(request.getHeader("X-App-Version"));
        HeaderUtils.setHeader(requestHeader);
        filterChain.doFilter(request, response);
    }

    private String getDomain(HttpServletRequest request) {
        String[] result = request.getRequestURL().toString().split("/");
        if (result.length < 2) {
            return "";
        }
        return result[0] + "//" + result[2];
    }

    /**
     * 从HTTP请求中获取指定名称的cookie的值。
     *
     * @param request    HttpServletRequest对象，用于获取HTTP请求信息。
     * @param cookieName 需要获取的cookie的名称。
     * @return 如果找到具有指定名称的cookie，则返回其值；否则返回null。
     */
    private String getCookieValue(HttpServletRequest request, String cookieName) {
        // 尝试获取客户端发送的所有cookie
        Cookie[] cookies = request.getCookies();

        // 检查cookies是否非空且至少包含一个cookie
        if (cookies != null && cookies.length > 0) {
            // 遍历所有cookie，查找名称匹配的cookie
            for (Cookie cookie : cookies) {
                if (cookieName.equalsIgnoreCase(cookie.getName())) {
                    // 找到匹配的cookie，返回其值
                    return cookie.getValue();
                }
            }
        }
        // 如果没有找到匹配的cookie，返回null
        return null;
    }


    private String getClientIP(HttpServletRequest request, String... otherHeaderNames) {
        String[] headers = new String[]{"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
        if (ArrayUtil.isNotEmpty(otherHeaderNames)) {
            headers = ArrayUtil.addAll(headers, otherHeaderNames);
        }
        return getClientIPByHeader(request, headers);
    }

    private String getClientIPByHeader(HttpServletRequest request, String... headerNames) {
        String ip;
        for (String header : headerNames) {
            ip = request.getHeader(header);
            if (!NetUtil.isUnknown(ip)) {
                return NetUtil.getMultistageReverseProxyIp(ip);
            }
        }
        ip = request.getRemoteAddr();
        return NetUtil.getMultistageReverseProxyIp(ip);
    }
}
