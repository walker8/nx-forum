package com.leyuz.uc.auth.exception;

import com.leyuz.common.security.ResultCode;
import com.leyuz.uc.auth.ResponseUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.MessageFormat;

/**
 * Spring Security 认证异常处理
 *
 * @author Walker
 * @date 2024/01/06
 */
@Component
public class MyAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        String msg = MessageFormat.format("请求访问：{0}，认证失败，无法访问系统资源", request.getRequestURI());
        ResponseUtils.writeErrMsg(response, ResultCode.builder().code(ResultCode.AUTH_FAILED_CODE).msg(msg).build());
    }
}
