package com.leyuz.common.filter;

import com.leyuz.common.context.QueryStatsContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Serial;

@Slf4j
public class QueryStatsFilter extends HttpFilter {
    @Serial
    private static final long serialVersionUID = -4696952439279374633L;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, jakarta.servlet.ServletException {
        try {
            chain.doFilter(request, response);
        } finally {
            QueryStatsContext.QueryStats stats = QueryStatsContext.get();
            if (stats.getDbQueryCount() > 0 || stats.getCacheHits() > 0) {
                String result = String.format("DB:%d, CacheHit:%d, CacheMiss:%d",
                        stats.getDbQueryCount(),
                        stats.getCacheHits(),
                        stats.getCacheMisses());
                String requestURI = ((HttpServletRequest) request).getRequestURI();
                String method = ((HttpServletRequest) request).getMethod();
                log.info("QueryStats: [{}]{}, {}", method, requestURI, result);
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.setHeader("X-Query-Stats", result);
            }
            QueryStatsContext.reset();
        }
    }
} 