package com.leyuz.uc.user;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.DesensitizedUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.common.exception.ValidationException;
import com.leyuz.common.ip.AddressUtils;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.common.mybatis.DataBaseUtils;
import com.leyuz.common.mybatis.PageQuery;
import com.leyuz.common.utils.HeaderUtils;
import com.leyuz.module.cache.GenericCache;
import com.leyuz.uc.auth.AuthUserDetails;
import com.leyuz.uc.config.LoginConfigApplication;
import com.leyuz.uc.config.RegisterConfigApplication;
import com.leyuz.uc.domain.auth.token.TokenGateway;
import com.leyuz.uc.domain.log.dataobject.LogTypeV;
import com.leyuz.uc.domain.log.dataobject.OperationStatusV;
import com.leyuz.uc.domain.user.UserE;
import com.leyuz.uc.domain.user.dataobject.AccountTypeV;
import com.leyuz.uc.domain.user.event.UserLoginEvent;
import com.leyuz.uc.domain.user.gateway.UserGateway;
import com.leyuz.uc.domain.user.service.UserDomainService;
import com.leyuz.uc.user.dto.*;
import com.leyuz.uc.user.mybatis.IUserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.leyuz.uc.user.LoginFailureService.MAX_FAILURE_COUNT;

@Service
@RequiredArgsConstructor
public class UserApplication {
    private final UserDomainService userDomainService;
    private final AuthenticationManager authenticationManager;
    private final TokenGateway tokenGateway;
    private final UserGateway userGateway;
    private final IUserService userService;
    private final VerifyCodeService verifyCodeService;
    private final RegisterConfigApplication registerConfigApplication;
    private final LoginConfigApplication loginConfigApplication;
    private final LoginFailureService loginFailureService;
    private final ApplicationEventPublisher eventPublisher;

    private static final List<String> orderByColumns = Arrays.asList("user_id", "create_time", "update_time", "last_active_date");

    private final GenericCache<Long, UserE> userIdCache;

    public void saveUserByAdmin(UserCmd userCmd) {
        saveUser(userCmd);
        // 记录用户创建日志 - 通过事件方式处理
        publishLogEvent(LogTypeV.INFO_UPDATE.getCode(), "管理员创建用户: " + userCmd.getUserName(), OperationStatusV.SUCCESS.getCode());
    }

    public void saveUser(UserCmd userCmd) {
        UserE userE = UserE.builder()
                .userName(userCmd.getUserName())
                .phone(userCmd.getPhone())
                .password(userCmd.getPassword())
                .email(userCmd.getEmail())
                .build();
        userDomainService.save(userE);
    }

    public void updateUserByAdmin(UserCmd userCmd) {
        UserE userE = UserE.builder()
                .userId(userCmd.getUserId())
                .userName(userCmd.getUserName())
                .phone(userCmd.getPhone())
                .password(userCmd.getPassword())
                .email(userCmd.getEmail())
                .build();
        userIdCache.remove(userE.getUserId());
        userDomainService.update(userE);

        // 记录用户更新日志 - 通过事件方式处理
        publishLogEvent(LogTypeV.INFO_UPDATE.getCode(), "管理员更新用户: " + userCmd.getUserName(), OperationStatusV.SUCCESS.getCode());
    }

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
            AuthUserDetails userDetails = (AuthUserDetails) authentication.getPrincipal();
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

    public void logout() {
        // 删除token
        tokenGateway.deleteByToken(HeaderUtils.getToken());
        publishLogEvent(LogTypeV.LOGOUT.getCode(), "用户退出登录", OperationStatusV.SUCCESS.getCode());
    }

    public UserVO getCurrentUser() {
        Long userId = HeaderUtils.getUserId();
        if (userId != null && userId > 0) {
            return getUserInfo(userId);
        } else {
            return null;
        }
    }

    public UserVO getUserInfo(Long userId) {
        if (userId != null && userId > 0) {
            UserE userE = userGateway.getById(userId);
            if (userE != null) {
                UserVO userVO = new UserVO();
                userVO.setUserName(userE.getUserName());
                userVO.setAvatar(userE.getAvatar());
                userVO.setUserId(userE.getUserId());
                userVO.setIntro(userE.getIntro());
                userVO.setCreateTime(userE.getCreateTime());
                userVO.setLastActiveDate(userE.getLastActiveDate());
                return userVO;
            }
        }
        throw new ValidationException("用户不存在或已注销");
    }

    public void updateCurrentUser(UserVO userVO) {
        Long userId = HeaderUtils.getUserId();
        if (userId != null && userId > 0) {
            UserE oldUserE = userGateway.getById(userId);
            UserE newUserE = UserE.builder()
                    .userId(userId)
                    .userName(userVO.getUserName())
                    .intro(userVO.getIntro())
                    .avatar(userVO.getAvatar())
                    .build();
            userIdCache.remove(userId);
            userDomainService.update(newUserE);
            publishUserInfoUpdateEvent(oldUserE, newUserE);
        }
    }

    private void publishUserInfoUpdateEvent(UserE oldUserE, UserE newUserE) {
        // 对比新老用户信息记录变更的内容到用户日志表中
        StringBuilder logContent = new StringBuilder();
        boolean hasChanges = false;

        if (!Objects.equals(oldUserE.getUserName(), newUserE.getUserName())) {
            logContent.append("用户名由[").append(oldUserE.getUserName()).append("]变更为[").append(newUserE.getUserName()).append("]；");
            hasChanges = true;
        }

        if (!Objects.equals(oldUserE.getIntro(), newUserE.getIntro())) {
            logContent.append("简介由[").append(oldUserE.getIntro() != null ? oldUserE.getIntro() : "").append("]变更为[")
                    .append(newUserE.getIntro() != null ? newUserE.getIntro() : "").append("]；");
            hasChanges = true;
        }

        if (!Objects.equals(oldUserE.getAvatar(), newUserE.getAvatar())) {
            logContent.append("头像已更新；");
            hasChanges = true;
        }

        if (hasChanges) {
            // 记录用户信息修改日志
            publishLogEvent(LogTypeV.INFO_UPDATE.getCode(), logContent.toString(), OperationStatusV.SUCCESS.getCode());
        }
    }

    public UserAccountVO getUserAccount() {
        Long userId = HeaderUtils.getUserId();
        if (userId != null && userId > 0) {
            UserPO userPO = userService.getById(userId);
            if (userPO != null) {
                UserAccountVO userVO = new UserAccountVO();
                // 邮箱脱敏
                userVO.setEmail(DesensitizedUtil.email(userPO.getEmail()));
                if (StringUtils.isNotEmpty(userPO.getPassword())) {
                    userVO.setPassword("***************");
                }
                userVO.setPhone(DesensitizedUtil.mobilePhone(userPO.getPhone()));
                return userVO;
            }
        }
        return null;
    }

    public CustomPage<AdminUserVO> queryUsersByAdmin(UserQuery query) {
        String orderByColumn = query.getOrderBy();
        if (StringUtils.isBlank(orderByColumn)) {
            orderByColumn = "user_id";
        }
        if (!orderByColumns.contains(orderByColumn)) {
            throw new ValidationException("排序字段不正确");
        }
        PageQuery pageQuery = PageQuery.builder()
                .pageNo(query.getPageNo())
                .pageSize(query.getPageSize())
                .orderByColumn(orderByColumn)
                .isAsc(false)
                .build();
        Map<String, Object> params = pageQuery.getParams();
        params.put("lastActiveIp", query.getLastActiveIp());
        params.put("userId", query.getUserId());
        params.put("userName", query.getUserName());
        Page<UserPO> userPOPage = userService.queryUsers(pageQuery);
        return DataBaseUtils.createCustomPage(userPOPage, this::toAdminUserVO);
    }

    private AdminUserVO toAdminUserVO(UserPO userPO) {
        AdminUserVO userVo = BeanUtil.toBean(userPO, AdminUserVO.class);
        userVo.setLocation(AddressUtils.getCityByIP(userPO.getLastActiveIp()));
        return userVo;
    }

    public void deleteUserByAdmin(Long userId) {
        if (userId == null || userId <= 0) {
            throw new ValidationException("用户ID不能为空");
        }
        UserE userE = userGateway.getById(userId);
        if (userE == null) {
            throw new ValidationException("用户不存在");
        }
        userIdCache.remove(userId);
        userGateway.deleteById(userId);

        // 记录用户删除日志 - 通过事件方式处理
        publishLogEvent(LogTypeV.INFO_UPDATE.getCode(), "管理员删除用户: " + userE.getUserName(), OperationStatusV.SUCCESS.getCode());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteUsersByAdmin(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }
        for (Long userId : userIds) {
            deleteUserByAdmin(userId);
        }
    }

    public UserE getByIdFromCache(long userId) {
        return userIdCache.computeIfAbsent(userId, key -> {
            UserE userE = userGateway.getById(key);
            if (userE == null) {
                userE = UserE.builder().userName("用户不存在").build();
            }
            userE.init();
            return userE;
        });
    }


    public UserE getByUserNameFromCache(String userName) {
        UserE userE = userGateway.getByUserName(userName);
        if (userE != null) {
            userE.init();
        }
        return userE;
    }


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
     * 注册用户(带验证码校验)
     */
    public void saveUserWithVerifyCode(UserCmd userCmd) {
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
        saveUser(userCmd);

        verifyCodeService.deleteCache(account, AccountTypeV.of(type), VerifyType.REGISTER);
    }


    public void checkUsername(String username) {
        userDomainService.checkUserName(username);
        registerConfigApplication.validateUsername(username);
    }

    public List<UserVO> queryUsername(String name) {
        List<UserPO> userPOList = userService.queryUsersByName(name, 8);
        // 转换为VO
        return userPOList.stream().map(userPO -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(userPO, userVO);
            return userVO;
        }).collect(Collectors.toList());
    }


    /**
     * 发布日志事件
     *
     * @param logType 日志类型代码
     * @param message 日志内容
     * @param status  操作状态代码
     */
    public void publishLogEvent(int logType, String message, int status) {
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
