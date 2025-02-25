package com.leyuz.uc.auth;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.fastjson2.JSON;
import com.leyuz.common.security.ResultCode;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;

/**
 * 响应工具类
 *
 * @author Walker
 * @date 2024/01/06
 */
public class ResponseUtils {

    /**
     * 异常消息返回(适用过滤器中处理异常响应)
     *
     * @param response
     * @param resultCode
     */
    public static void writeErrMsg(HttpServletResponse response, ResultCode resultCode) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        if (ResultCode.AUTH_FAILED_CODE.equals(resultCode.getCode())) {
            // 请求需要有效的身份验证凭据，但客户端未提供或提供的凭据无效。
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } else if (ResultCode.PERMISSION_DENIED_CODE.equals(resultCode.getCode())) {
            // 服务器已完成身份验证，但客户端（即使已提供有效的身份验证信息）不具备访问该资源所需的权限。
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
        response.getWriter().print(JSON.toJSONString(SingleResponse.buildFailure(resultCode.getCode(), resultCode.getMsg())));
    }


}
