package com.leyuz.uc.user.auth;

import cn.hutool.core.date.DateUtil;
import com.leyuz.common.exception.ValidationException;
import com.leyuz.common.utils.HeaderUtils;
import com.leyuz.uc.config.LoginConfigApplication;
import com.leyuz.uc.config.RegisterConfigApplication;
import com.leyuz.uc.auth.token.TokenGateway;
import com.leyuz.uc.user.UserE;
import com.leyuz.uc.user.dataobject.AccountTypeV;
import com.leyuz.uc.user.event.UserLoginEvent;
import com.leyuz.uc.user.service.UserDomainService;
import com.leyuz.uc.log.dto.LogTypeV;
import com.leyuz.uc.log.dto.OperationStatusV;
import com.leyuz.uc.user.dto.UserCmd;
import com.leyuz.uc.user.auth.dto.UserLoginDTO;
import com.leyuz.uc.user.dto.UserResp;
import com.leyuz.uc.user.verify.dto.VerifyType;
import com.leyuz.uc.user.verify.VerifyCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.UUID;

import static com.leyuz.uc.user.auth.LoginFailureService.MAX_FAILURE_COUNT;

/**
 * 认证应用服务
 * 负责用户登录、注册、登出等认证相关功能
 *
 * @author walker
 * @since 2025-10-25
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthApplication {
    private final UserDomainService userDomainService;
    private final AuthenticationManager authenticationManager;
    private final TokenGateway tokenGateway;
    private final VerifyCodeService verifyCodeService;
    private final RegisterConfigApplication registerConfigApplication;
    private final LoginConfigApplication loginConfigApplication;
    private final LoginFailureService loginFailureService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 用户登录
     *
     * @param userLoginDTO 用户登录信息数据传输对象，包含用户名和密码
     * @return UserResp 用户登录响应对象，包含访问令牌
     * @throws ValidationException 如果用户名为空，抛出验证异常
     */
    public UserResp login(UserLoginDTO userLoginDTO) {
        // 验证登录配置
        loginConfigApplication.validatePasswordLoginEnabled();
        String userName = userLoginDTO.getUserName();
        if (StringUtils.isBlank(userName)) {
            throw new ValidationException("请输入用户名/手机号码/邮箱地址");
        }
        // 检查是否被锁定
        if (loginFailureService.isUserLocked(userName)) {
            String message = "账户已被锁定，请30分钟后再试";
            // 记录登录失败日志 - 通过事件方式处理
            publishLogEvent(LogTypeV.LOGIN.getCode(), message, OperationStatusV.FAILURE.getCode());
            throw new ValidationException(message);
        }
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userLoginDTO.getUserName(), userLoginDTO.getPassword());
        try {
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            com.leyuz.uc.auth.AuthUserDetails userDetails = (com.leyuz.uc.auth.AuthUserDetails) authentication.getPrincipal();
            loginFailureService.clearLoginFailure(userName);

            // 记录登录成功日志 - 通过事件方式处理
            HeaderUtils.setUser(userDetails.getUserId(), null);
            publishLogEvent(LogTypeV.LOGIN.getCode(), "用户通过账密登录", OperationStatusV.SUCCESS.getCode());

            return getUserResp(userDetails.getUserId());
        } catch (BadCredentialsException e) {
            // 记录登录失败
            int failCount = loginFailureService.recordLoginFailure(userName);
            String message;
            if (failCount >= MAX_FAILURE_COUNT) {
                message = "登录失败次数过多，账户已被锁定30分钟";
            } else {
                message = MessageFormat.format("用户名或密码错误，您还有{0}次机会", MAX_FAILURE_COUNT - failCount);
            }

            // 记录登录失败日志 - 通过事件方式处理
            publishLogEvent(LogTypeV.LOGIN.getCode(), message + "，账号：" + userName, OperationStatusV.FAILURE.getCode());

            if (failCount >= MAX_FAILURE_COUNT) {
                throw new ValidationException(message);
            }
            throw new UsernameNotFoundException(message);
        }
    }

    /**
     * 用户登出
     */
    public void logout() {
        // 删除token
        tokenGateway.deleteByToken(HeaderUtils.getToken());
        publishLogEvent(LogTypeV.LOGOUT.getCode(), "用户退出登录", OperationStatusV.SUCCESS.getCode());
    }

    /**
     * 注册用户(带验证码校验)
     */
    public void register(UserCmd userCmd) {
        // 检查是否允许注册
        registerConfigApplication.validateRegisterEnabled();
        // 验证验证码
        String account = userCmd.getPhone();
        String type = "phone";
        if (StringUtils.isBlank(account)) {
            account = userCmd.getEmail();
            type = "email";
        }

        // 先校验验证码
        verifyCodeService.verifyCode(account, userCmd.getVerifyCode(), AccountTypeV.of(type), VerifyType.REGISTER);

        registerConfigApplication.validateUsername(userCmd.getUserName());
        // 执行密码复杂度校验
        registerConfigApplication.validatePassword(userCmd.getPassword());

        // 调用原有注册逻辑
        UserE userE = UserE.builder()
                .userName(userCmd.getUserName())
                .phone(userCmd.getPhone())
                .password(userCmd.getPassword())
                .email(userCmd.getEmail())
                .build();
        userDomainService.save(userE);

        verifyCodeService.deleteCache(account, AccountTypeV.of(type), VerifyType.REGISTER);
    }

    /**
     * 生成用户响应对象（包含Token）
     */
    private UserResp getUserResp(Long userId) {
        UserResp userResp = new UserResp();
        userResp.setUserId(userId);
        userResp.setTokenExpiresIn(DateUtil.offsetDay(DateUtil.date(), 365).getTime());
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenGateway.saveToken(userResp.getUserId(), token);
        userResp.setToken(token);
        return userResp;
    }

    /**
     * 发布日志事件
     *
     * @param logType 日志类型代码
     * @param message 日志内容
     * @param status  操作状态代码
     */
    private void publishLogEvent(int logType, String message, int status) {
        // 创建并发布日志事件
        UserLoginEvent loginEvent = UserLoginEvent.builder()
                .userId(HeaderUtils.getUserId())
                .logType(logType)
                .logContent(message)
                .ipAddress(HeaderUtils.getIp())
                .userAgent(HeaderUtils.getUserAgent())
                .operationStatus(status)
                .build();
        eventPublisher.publishEvent(loginEvent);
    }
}

