package com.leyuz.uc.auth.filter;

import com.leyuz.common.security.ResultCode;
import com.leyuz.common.utils.HeaderUtils;
import com.leyuz.uc.auth.AuthUserDetails;
import com.leyuz.uc.auth.ResponseUtils;
import com.leyuz.uc.user.auth.TokenApplication;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * token 过滤器
 *
 * @author Walker
 * @since 2024/1/7
 */
@Slf4j
public class TokenFilter extends OncePerRequestFilter {

    private final TokenApplication tokenApplication;

    public TokenFilter(TokenApplication tokenApplication) {
        this.tokenApplication = tokenApplication;
    }

    /**
     * 从请求中获取 Token，校验 Token 是否合法
     * <p>
     * 如果合法则将 Authentication 设置到 Spring Security Context 上下文中
     * 如果不合法则清空 Spring Security Context 上下文，并直接返回响应
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException {
        try {
            if (StringUtils.isNotBlank(HeaderUtils.getToken())) {
                // token有效正常访问
                Long userId = tokenApplication.getUserIdByToken(HeaderUtils.getToken());
                if (userId > 0) {
                    AuthUserDetails authUserDetails = new AuthUserDetails();
                    authUserDetails.setUserId(userId);
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(authUserDetails, "", Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    HeaderUtils.setUser(userId, null);
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            //this is very important, since it guarantees the user is not authenticated at all
            SecurityContextHolder.clearContext();
            if (ex instanceof SignatureException || ex instanceof MalformedJwtException) {
                ResponseUtils.writeErrMsg(response, ResultCode.builder().code(ResultCode.AUTH_FAILED_CODE).msg("签名校验失败").build());
            } else if (ex instanceof ExpiredJwtException) {
                ResponseUtils.writeErrMsg(response, ResultCode.builder().code(ResultCode.AUTH_FAILED_CODE).msg("签名已过期").build());
            } else {
                log.error("TokenFilter Error", ex);
                ResponseUtils.writeErrMsg(response, ResultCode.builder().code(ResultCode.AUTH_FAILED_CODE).msg(ex.getMessage()).build());
            }
        }
    }
}
