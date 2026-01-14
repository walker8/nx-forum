package com.leyuz.bbs.filter;

import com.leyuz.bbs.system.access.AccessLogApplication;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 访问日志过滤器
 *
 * @author Walker
 * @since 2025-01-11
 */
@Slf4j
@RequiredArgsConstructor
public class AccessLogFilter extends OncePerRequestFilter {

    private final AccessLogApplication accessLogApplication;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        // 排除静态资源和健康检查
        if (shouldExclude(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 异步记录访问日志（传递参数而不是request对象）
            accessLogApplication.logAccessAsync();
        }
    }

    /**
     * 判断是否应该排除（不记录日志）
     *
     * @param uri 请求URI
     * @return true-不记录，false-记录
     */
    private boolean shouldExclude(String uri) {
        return uri.contains("/swagger") ||
                uri.contains("/api-docs") ||
                uri.contains("/actuator") ||
                uri.endsWith(".js") ||
                uri.endsWith(".css") ||
                uri.endsWith(".png") ||
                uri.endsWith(".jpg") ||
                uri.endsWith(".jpeg") ||
                uri.endsWith(".gif") ||
                uri.endsWith(".ico") ||
                uri.endsWith(".svg") ||
                uri.endsWith(".woff") ||
                uri.endsWith(".woff2") ||
                uri.endsWith(".ttf") ||
                uri.endsWith(".eot");
    }
}
