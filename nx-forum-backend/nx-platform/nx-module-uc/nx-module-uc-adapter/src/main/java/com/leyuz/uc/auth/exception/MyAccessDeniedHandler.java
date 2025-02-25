package com.leyuz.uc.auth.exception;

import com.leyuz.common.security.ResultCode;
import com.leyuz.uc.auth.ResponseUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Spring Security访问异常处理器
 *
 * @author Walker
 * @date 2024/01/06
 */
@Component
public class MyAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        ResponseUtils.writeErrMsg(response, ResultCode.builder().code(ResultCode.PERMISSION_DENIED_CODE).msg("权限不足").build());
    }
}
