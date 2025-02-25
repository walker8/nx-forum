package com.leyuz.uc.config;

import com.alibaba.fastjson2.JSON;
import com.leyuz.common.exception.ValidationException;
import com.leyuz.module.config.app.ConfigApplication;
import com.leyuz.uc.config.dto.LoginConfigDTO;
import com.leyuz.uc.config.dto.LoginConfigVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginConfigApplication implements ApplicationRunner {

    private final ConfigApplication configApplication;
    private static final String LOGIN_CONFIG_KEY = "uc_login_config";

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 初始化默认配置
        if (getLoginConfig() == null) {
            updateLoginConfig(new LoginConfigDTO());
        }
    }

    /**
     * 更新登录配置
     */
    public void updateLoginConfig(LoginConfigDTO config) {
        if (config != null) {
            validateConfig(config);
            configApplication.updateConfig(LOGIN_CONFIG_KEY, JSON.toJSONString(config));
        }
    }

    /**
     * 获取登录配置
     */
    public LoginConfigDTO getLoginConfig() {
        return configApplication.getConfigValueByKey(LOGIN_CONFIG_KEY, LoginConfigDTO.class);
    }

    /**
     * 获取登录配置
     */
    public LoginConfigVO getLoginConfigVO() {
        LoginConfigDTO loginConfigDTO = getLoginConfig();
        LoginConfigVO loginConfigVO = new LoginConfigVO();
        BeanUtils.copyProperties(loginConfigDTO, loginConfigVO);
        return loginConfigVO;
    }

    private void validateConfig(LoginConfigDTO config) {
        // 至少启用一种登录方式
        if (!config.isEnableEmailCodeLogin() && !config.isEnablePhoneCodeLogin() && !config.isEnablePasswordLogin()) {
            throw new ValidationException("至少启用一种登录方式");
        }

        // 至少启用一种找回密码方式
        if (!config.isEnableEmailResetPassword() && !config.isEnablePhoneResetPassword()) {
            throw new ValidationException("至少启用一种找回密码方式");
        }

        if (config.getMaxLoginFailCount() < 1) {
            throw new ValidationException("登录失败次数限制不能小于1次");
        }

        if (config.getLoginLockMinutes() < 1) {
            throw new ValidationException("登录锁定时间不能小于1分钟");
        }
    }

    public void validatePhoneCodeLoginEnabled() {
        LoginConfigDTO config = getLoginConfig();
        if (config != null && !config.isEnablePhoneCodeLogin()) {
            throw new ValidationException("短信登录功能未开启");
        }
    }

    public void validateEmailCodeLoginEnabled() {
        LoginConfigDTO config = getLoginConfig();
        if (config != null && !config.isEnableEmailCodeLogin()) {
            throw new ValidationException("邮箱登录功能未开启");
        }
    }

    public void validatePasswordLoginEnabled() {
        LoginConfigDTO config = getLoginConfig();
        if (config != null && !config.isEnablePasswordLogin()) {
            throw new ValidationException("密码登录功能未开启");
        }
    }

    public void validateEmailResetPasswordEnabled() {
        LoginConfigDTO config = getLoginConfig();
        if (config != null && !config.isEnableEmailResetPassword()) {
            throw new ValidationException("邮箱找回密码功能未开启");
        }
    }

    public void validatePhoneResetPasswordEnabled() {
        LoginConfigDTO config = getLoginConfig();
        if (config != null && !config.isEnablePhoneResetPassword()) {
            throw new ValidationException("手机找回密码功能未开启");
        }
    }
} 