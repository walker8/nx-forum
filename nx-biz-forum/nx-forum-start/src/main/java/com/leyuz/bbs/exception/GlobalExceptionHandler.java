package com.leyuz.bbs.exception;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyuz.common.exception.AuditException;
import com.leyuz.common.exception.ValidationException;
import com.leyuz.common.security.ResultCode;
import com.leyuz.common.utils.HeaderUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

@RestController
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(value = BizException.class)
    public SingleResponse bizExceptionHandler(BizException e) {
        return SingleResponse.buildFailure(ResultCode.BUSINESS_ERROR_CODE, e.getMessage());
    }

    @ExceptionHandler(value = SQLException.class)
    public SingleResponse sqlExceptionHandler(SQLException e) {
        return SingleResponse.buildFailure(ResultCode.SERVER_ERROR_CODE, e.getMessage());
    }

    @ExceptionHandler(value = ValidationException.class)
    public SingleResponse exceptionHandler(ValidationException e) {
        return SingleResponse.buildFailure(ResultCode.VALIDATION_ERROR_CODE, e.getMessage());
    }

    @ExceptionHandler(value = UsernameNotFoundException.class)
    public SingleResponse exceptionHandler(UsernameNotFoundException e) {
        return SingleResponse.buildFailure(ResultCode.AUTH_FAILED_CODE, e.getMessage());
    }

    @ExceptionHandler(value = AuditException.class)
    public SingleResponse exceptionHandler(AuditException e) {
        // 审核异常时定制化返回，方便前端定制化渲染
        SingleResponse<String> response = SingleResponse.of(e.getMessage());
        response.setErrCode(ResultCode.BUSINESS_ERROR_CODE);
        return response;
    }

    @ExceptionHandler(value = BadCredentialsException.class)
    public SingleResponse exceptionHandler(BadCredentialsException e) {
        // security错误
        return SingleResponse.buildFailure(ResultCode.AUTH_FAILED_CODE, e.getMessage());
    }

    @ExceptionHandler(value = AuthorizationDeniedException.class)
    public SingleResponse exceptionHandler(AuthorizationDeniedException e) {
        // 无权限错误
        String errMessage = "您当前所在用户组无权限操作";
        if (!e.getMessage().equalsIgnoreCase("Access Denied")) {
            errMessage = "您当前所在用户组" + e.getMessage();
            if (HeaderUtils.getUserId() == null || HeaderUtils.getUserId() == 0) {
                errMessage = "游客" + e.getMessage() + "，请先登录账号";
            }
        }
        return SingleResponse.buildFailure(ResultCode.PERMISSION_DENIED_CODE, errMessage);
    }

    @ExceptionHandler(value = Exception.class)
    public SingleResponse exceptionHandler(Exception e) {
        log.error("", e);
        // 线上统一报服务器异常
        return SingleResponse.buildFailure(ResultCode.SERVER_ERROR_CODE, e.getMessage());
    }
}
