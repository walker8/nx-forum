package com.leyuz.uc.user.verify;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.leyuz.common.exception.ValidationException;
import com.leyuz.common.utils.HeaderUtils;
import com.leyuz.module.cache.GenericCache;
import com.leyuz.uc.config.LoginConfigApplication;
import com.leyuz.uc.config.RegisterConfigApplication;
import com.leyuz.uc.auth.token.TokenGateway;
import com.leyuz.uc.user.UserE;
import com.leyuz.uc.user.dataobject.AccountTypeV;
import com.leyuz.uc.user.gateway.UserGateway;
import com.leyuz.uc.user.service.UserDomainService;
import com.leyuz.uc.user.verify.dto.ChangeToNewEmailCmd;
import com.leyuz.uc.user.dto.UserResp;
import com.leyuz.uc.user.auth.dto.VerifyCodeLoginDTO;
import com.leyuz.uc.user.verify.dto.VerifyCurrentEmailCmd;
import com.leyuz.uc.user.verify.dto.VerifyType;

import cn.hutool.core.date.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 验证码应用服务
 * 负责验证码相关的业务逻辑处理
 *
 * @author walker
 * @since 2025-10-04
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class VerifyCodeApplication {

    private final UserGateway userGateway;
    private final UserDomainService userDomainService;
    private final VerifyCodeService verifyCodeService;
    private final RegisterConfigApplication registerConfigApplication;
    private final LoginConfigApplication loginConfigApplication;
    private final TokenGateway tokenGateway;
    private final GenericCache<Long, UserE> userIdCache;
    private final GenericCache<Long, String> emailChangeStepCache;

    /**
     * 发送邮箱验证码
     *
     * @param email 邮箱地址
     * @param type  验证码类型(register/login/reset_password)
     */
    public void sendEmailVerifyCode(String email, String type) {
        sendEmailVerifyCode(email, type, null);
    }

    /**
     * 发送邮箱验证码 (支持换绑邮箱场景)
     *
     * @param email  邮箱地址 (当type=change_email且target=current时可为空，后端自动获取)
     * @param type   验证码类型(register/login/reset_password/change_email)
     * @param target 当type=change_email时有效：current(发送到当前用户邮箱)、new(发送到新邮箱)
     */
    public void sendEmailVerifyCode(String email, String type, String target) {
        VerifyType verifyType = VerifyType.of(type);
        checkEmailVerifyCodeEnabled(verifyType);

        // 处理换绑邮箱场景
        if (VerifyType.CHANGE_EMAIL.equals(verifyType)) {
            handleChangeEmailVerifyCode(email, target);
            return;
        }

        if (StringUtils.isBlank(email)) {
            throw new ValidationException("请输入邮箱地址");
        }

        // 处理其他场景（注册、登录、重置密码）
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
     * 处理换绑邮箱验证码发送逻辑
     */
    private void handleChangeEmailVerifyCode(String email, String target) {
        Long userId = HeaderUtils.getUserId();
        if (userId == null || userId <= 0) {
            throw new ValidationException("请先登录再进行此操作！");
        }
        UserE currentUser = getByIdFromCache(userId);
        String userName = currentUser.getUserName();

        if ("current".equals(target)) {
            // 发送到当前用户邮箱 (忽略传入的email参数，后端自动获取)
            String currentEmail = currentUser.getEmail();
            if (StringUtils.isBlank(currentEmail)) {
                throw new ValidationException("用户邮箱为空，无法发送验证码");
            }
            verifyCodeService.sendEmailVerifyCode(currentEmail, userName, VerifyType.CHANGE_EMAIL);

        } else if ("new".equals(target)) {
            // 发送到新邮箱
            if (StringUtils.isBlank(email)) {
                throw new ValidationException("请输入新邮箱地址");
            }
            // 检查当前邮箱验证是否已通过
            String verificationStatus = emailChangeStepCache.get(currentUser.getUserId());
            if (!"verified".equals(verificationStatus)) {
                throw new ValidationException("请先完成当前邮箱验证");
            }
            // 检查新邮箱是否可用
            validateNewEmail(email, currentUser.getUserId());
            verifyCodeService.sendEmailVerifyCode(email, userName, VerifyType.CHANGE_EMAIL);

        } else {
            throw new ValidationException("无效的target参数，换绑邮箱场景下target必须为'current'或'new'");
        }
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

    /**
     * 验证码登录
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
        // 生成token
        UserResp resp = getUserResp(userE.getUserId());
        verifyCodeService.deleteCache(account, AccountTypeV.of(type), VerifyType.LOGIN);
        return resp;
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

    /**
     * 验证当前邮箱
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
    }

    /**
     * 设置新邮箱
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

    private UserE getByIdFromCache(Long userId) {
        UserE userE = userIdCache.get(userId);
        if (userE == null) {
            userE = userGateway.getById(userId);
            if (userE != null) {
                userE.init();
                userIdCache.put(userId, userE);
            }
        }
        return userE;
    }

    private UserResp getUserResp(Long userId) {
        UserE userE = getByIdFromCache(userId);
        if (userE == null) {
            throw new ValidationException("用户不存在");
        }

        UserResp userResp = new UserResp();
        userResp.setUserId(userE.getUserId());
        userResp.setTokenExpiresIn(DateUtil.offsetDay(DateUtil.date(), 365).getTime());
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenGateway.saveToken(userResp.getUserId(), token);
        userResp.setToken(token);
        return userResp;
    }
}
