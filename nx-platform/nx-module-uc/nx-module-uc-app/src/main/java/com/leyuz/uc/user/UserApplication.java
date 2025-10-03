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
    private final GenericCache<Long, String> emailChangeStepCache;

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

    /**
     * 发送邮箱验证码
     *
     * @param email 邮箱地址
     * @param type  验证码类型(register/login/reset-password)
     */
    public void sendEmailVerifyCode(String email, String type) {
        VerifyType verifyType = VerifyType.of(type);
        checkEmailVerifyCodeEnabled(verifyType);
        if (StringUtils.isBlank(email)) {
            throw new ValidationException("请输入邮箱地址");
        }
        UserE userE = userGateway.getByEmail(email);
        String userName = "";
        if (verifyType.equals(VerifyType.REGISTER)) {
            if (userE != null) {
                throw new ValidationException("邮箱已注册，请直接登录");
            }
        } else {
            // 当类型是登录或重置密码时，需要检查用户是否存在
            if (userE == null) {
                throw new ValidationException("邮箱未注册");
            } else {
                userName = userE.getUserName();
            }
        }
        verifyCodeService.sendEmailVerifyCode(email, userName, verifyType);
    }

    /**
     * 发送验证码到当前登录用户的邮箱
     */
    public void sendCurrentUserEmailVerifyCode() {
        UserE userE = getByIdFromCache(HeaderUtils.getUserId());

        String email = userE.getEmail();
        if (StringUtils.isBlank(email)) {
            throw new ValidationException("用户邮箱为空，无法发送验证码");
        }

        verifyCodeService.sendEmailVerifyCode(email, userE.getUserName(), VerifyType.CHANGE_EMAIL);
    }

    /**
     * 发送验证码到新邮箱（用于邮箱换绑）
     *
     * @param newEmail 新邮箱地址
     */
    public void sendNewEmailVerifyCode(String newEmail) {
        UserE currentUser = getByIdFromCache(HeaderUtils.getUserId());
        // 检查当前邮箱验证是否已通过
        String verificationStatus = emailChangeStepCache.get(currentUser.getUserId());
        if (!"verified".equals(verificationStatus)) {
            throw new ValidationException("请先完成当前邮箱验证");
        }
        // 检查新邮箱是否可用
        validateNewEmail(newEmail, currentUser.getUserId());

        verifyCodeService.sendEmailVerifyCode(newEmail, currentUser.getUserName(), VerifyType.CHANGE_EMAIL);
    }

    /**
     * 发送手机验证码
     *
     * @param phone 手机号码
     * @param type  验证码类型(register/login/reset-password)
     */
    public void sendPhoneVerifyCode(String phone, String type) {
        VerifyType verifyType = VerifyType.of(type);
        checkPhoneVerifyCodeEnabled(verifyType);
        if (StringUtils.isBlank(phone)) {
            throw new ValidationException("请输入手机号码");
        }
        UserE userE = userGateway.getByPhone(phone);
        if (verifyType.equals(VerifyType.REGISTER)) {
            if (userE != null) {
                throw new ValidationException("手机号码已被使用");
            }
        } else {
            // 当类型是登录或重置密码时，需要检查用户是否存在
            if (userE == null) {
                throw new ValidationException("手机号码未注册");
            }
        }
        verifyCodeService.sendPhoneVerifyCode(phone, verifyType);
    }

    private void checkPhoneVerifyCodeEnabled(VerifyType verifyType) {
        switch (verifyType) {
            case REGISTER -> registerConfigApplication.validateSmsRegisterEnabled();
            case LOGIN -> loginConfigApplication.validatePhoneCodeLoginEnabled();
            case RESET_PASSWORD -> loginConfigApplication.validatePhoneResetPasswordEnabled();
            case CHANGE_EMAIL -> {
                // 邮箱换绑功能默认启用，无需额外配置检查
            }
        }
    }

    private void checkEmailVerifyCodeEnabled(VerifyType verifyType) {
        switch (verifyType) {
            case REGISTER -> registerConfigApplication.validateEmailRegisterEnabled();
            case LOGIN -> loginConfigApplication.validateEmailCodeLoginEnabled();
            case RESET_PASSWORD -> loginConfigApplication.validateEmailResetPasswordEnabled();
            case CHANGE_EMAIL -> {
                // 邮箱换绑功能默认启用，无需额外配置检查
            }
        }
    }

    /**
     * 验证码登录
     *
     * @param loginDTO 登录信息
     */
    public UserResp verifyCodeLogin(VerifyCodeLoginDTO loginDTO) {
        String account = loginDTO.getAccount(); // 手机号或邮箱
        String code = loginDTO.getCode();
        String type = loginDTO.getType(); // phone/email

        if (StringUtils.isBlank(account)) {
            throw new ValidationException("请输入手机号或邮箱");
        }
        if (StringUtils.isBlank(code)) {
            throw new ValidationException("请输入验证码");
        }

        // 验证验证码
        verifyCodeService.verifyCode(account, code, AccountTypeV.of(type), VerifyType.LOGIN);

        // 获取用户信息
        UserE userE;
        if ("phone".equals(type)) {
            userE = userGateway.getByPhone(account);
        } else {
            userE = userGateway.getByEmail(account);
        }

        if (userE == null) {
            throw new ValidationException("用户不存在或已注销");
        }

        HeaderUtils.setUser(userE.getUserId(), null);
        publishLogEvent(LogTypeV.LOGIN.getCode(), "用户通过验证码登录，手机号或邮箱: " + account, OperationStatusV.SUCCESS.getCode());

        // 生成token
        UserResp resp = getUserResp(userE.getUserId());
        verifyCodeService.deleteCache(account, AccountTypeV.of(type), VerifyType.LOGIN);
        return resp;
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

    /**
     * 通过验证码重置密码
     *
     * @param account     手机号或邮箱
     * @param code        验证码
     * @param newPassword 新密码
     * @param type        账号类型(phone/email)
     */
    public void resetPasswordByVerifyCode(String account, String code, String newPassword, String type) {
        if (StringUtils.isBlank(account)) {
            throw new ValidationException("请输入手机号或邮箱");
        }
        if (StringUtils.isBlank(code)) {
            throw new ValidationException("请输入验证码");
        }
        if (StringUtils.isBlank(newPassword)) {
            throw new ValidationException("请输入新密码");
        }

        // 验证验证码
        verifyCodeService.verifyCode(account, code, AccountTypeV.of(type), VerifyType.RESET_PASSWORD);

        registerConfigApplication.validatePassword(newPassword);

        // 获取用户信息
        UserE userE;
        if ("phone".equals(type)) {
            userE = userGateway.getByPhone(account);
        } else {
            userE = userGateway.getByEmail(account);
        }

        if (userE == null) {
            throw new ValidationException("用户不存在");
        }

        // 更新密码
        UserE newUserE = UserE.builder()
                .userId(userE.getUserId())
                .password(newPassword)
                .build();
        userDomainService.update(newUserE);

        verifyCodeService.deleteCache(account, AccountTypeV.of(type), VerifyType.RESET_PASSWORD);
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
     * 验证当前邮箱
     *
     * @param cmd 验证命令
     */
    public void verifyCurrentEmail(VerifyCurrentEmailCmd cmd) {
        UserE userE = getByIdFromCache(HeaderUtils.getUserId());

        String currentEmail = userE.getEmail();
        if (StringUtils.isBlank(currentEmail)) {
            throw new ValidationException("当前用户没有绑定邮箱");
        }

        // 验证验证码
        verifyCodeService.verifyCode(currentEmail, cmd.getVerifyCode(), AccountTypeV.EMAIL, VerifyType.CHANGE_EMAIL);

        // 验证成功后删除验证码缓存
        verifyCodeService.deleteCache(currentEmail, AccountTypeV.EMAIL, VerifyType.CHANGE_EMAIL);

        // 设置当前邮箱验证通过的标记，有效期15分钟
        emailChangeStepCache.put(userE.getUserId(), "verified", 900L);

        publishLogEvent(LogTypeV.INFO_UPDATE.getCode(),
                "邮箱换绑第一步验证成功，当前邮箱：" + DesensitizedUtil.email(currentEmail),
                OperationStatusV.SUCCESS.getCode());
    }

    /**
     * 设置新邮箱
     *
     * @param cmd 换绑命令
     */
    @Transactional(rollbackFor = Exception.class)
    public void changeToNewEmail(ChangeToNewEmailCmd cmd) {
        Long userId = HeaderUtils.getUserId();

        // 检查当前邮箱验证是否已通过
        String verificationStatus = emailChangeStepCache.get(userId);
        if (!"verified".equals(verificationStatus)) {
            throw new ValidationException("请先完成当前邮箱验证");
        }

        String newEmail = cmd.getNewEmail();
        // 检查新邮箱是否可用
        validateNewEmail(newEmail, userId);

        // 验证新邮箱验证码
        verifyCodeService.verifyCode(newEmail, cmd.getVerifyCode(), AccountTypeV.EMAIL, VerifyType.CHANGE_EMAIL);

        // 更新用户邮箱
        UserE updateUserE = UserE.builder()
                .userId(userId)
                .email(newEmail)
                .build();
        userDomainService.update(updateUserE);

        // 清除缓存
        userIdCache.remove(userId);
        verifyCodeService.deleteCache(newEmail, AccountTypeV.EMAIL, VerifyType.CHANGE_EMAIL);

        // 清除当前邮箱验证标记
        emailChangeStepCache.remove(userId);

        publishLogEvent(LogTypeV.INFO_UPDATE.getCode(), "邮箱换绑成功：" + DesensitizedUtil.email(newEmail), OperationStatusV.SUCCESS.getCode());
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

    /**
     * 检查新邮箱是否可用
     *
     * @param newEmail      新邮箱地址
     * @param currentUserId 当前用户ID
     */
    private void validateNewEmail(String newEmail, Long currentUserId) {
        if (StringUtils.isBlank(newEmail)) {
            throw new ValidationException("请输入新邮箱地址");
        }

        UserE existingUser = userGateway.getByEmail(newEmail);
        if (existingUser != null) {
            if (existingUser.getUserId().equals(currentUserId)) {
                throw new ValidationException("该邮箱已绑定当前用户");
            } else {
                throw new ValidationException("该邮箱已被其他用户绑定");
            }
        }
    }
}
