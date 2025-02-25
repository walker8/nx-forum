package com.leyuz.uc.config;

import com.alibaba.fastjson2.JSON;
import com.leyuz.common.exception.ValidationException;
import com.leyuz.module.config.app.ConfigApplication;
import com.leyuz.uc.config.dto.RegisterConfigDTO;
import com.leyuz.uc.config.dto.RegisterConfigVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterConfigApplication implements ApplicationRunner {

    private final ConfigApplication configApplication;
    private static final String REGISTER_CONFIG_KEY = "uc_register_config";

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 初始化默认配置
        if (getRegisterConfig() == null) {
            RegisterConfigDTO defaultConfig = new RegisterConfigDTO();
            defaultConfig.setForbiddenUsernames(new HashSet<>());
            updateRegisterConfig(defaultConfig);
        }
    }

    /**
     * 更新注册配置
     */
    public void updateRegisterConfig(RegisterConfigDTO config) {
        if (config != null) {
            // 验证配置的有效性
            validateConfig(config);
            configApplication.updateConfig(REGISTER_CONFIG_KEY, JSON.toJSONString(config));
        }
    }

    /**
     * 获取注册配置
     */
    public RegisterConfigDTO getRegisterConfig() {
        return configApplication.getConfigValueByKey(REGISTER_CONFIG_KEY, RegisterConfigDTO.class);
    }

    /**
     * 获取注册配置
     */
    public RegisterConfigVO getRegisterConfigVO() {
        RegisterConfigDTO registerConfigDTO = getRegisterConfig();
        RegisterConfigVO registerConfigVO = new RegisterConfigVO();
        BeanUtils.copyProperties(registerConfigDTO, registerConfigVO);
        return registerConfigVO;
    }

    /**
     * 检查用户名是否被禁止
     */
    public boolean isUsernameForbidden(String username) {
        if (StringUtils.isEmpty(username)) {
            return false;
        }
        RegisterConfigDTO config = getRegisterConfig();
        if (config == null || config.getForbiddenUsernames() == null || config.getForbiddenUsernames().isEmpty()) {
            return false;
        }

        return config.getForbiddenUsernames().stream()
                .anyMatch(username::contains);
    }

    /**
     * 验证用户名是否符合规则
     */
    public void validateUsername(String username) {
        if (isUsernameForbidden(username)) {
            throw new ValidationException("用户名不允许使用");
        }
    }

    /**
     * 验证密码是否符合规则
     */
    public void validatePassword(String password) {
        RegisterConfigDTO config = getRegisterConfig();
        if (config == null) {
            return;
        }

        if (password.length() < config.getPasswordMinLength()) {
            throw new ValidationException("密码长度不能小于" + config.getPasswordMinLength());
        }
        if (password.length() > config.getPasswordMaxLength()) {
            throw new ValidationException("密码长度不能大于" + config.getPasswordMaxLength());
        }

        if (config.getPasswordStrength() == 1) {
            if (!password.matches(".*[a-zA-Z].*") || !password.matches(".*\\d.*")) {
                throw new ValidationException("密码必须包含数字和字母");
            }
        } else if (config.getPasswordStrength() == 2) {
            if (!password.matches(".*[a-zA-Z].*") || !password.matches(".*\\d.*")
                    || !password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
                throw new ValidationException("密码必须包含数字、字母和特殊字符");
            }
        }
    }

    private void validateConfig(RegisterConfigDTO config) {
        if (config.isEnableRegister() && !config.isEnableEmailRegister() && !config.isEnableSmsRegister()) {
            throw new ValidationException("至少启用一种注册方式");
        }

        if (config.getPasswordMinLength() < 1) {
            throw new ValidationException("密码最小长度不能小于1");
        }
        if (config.getPasswordMaxLength() < config.getPasswordMinLength()) {
            throw new ValidationException("密码最大长度不能小于最小长度");
        }
        if (config.getPasswordStrength() < 0 || config.getPasswordStrength() > 2) {
            throw new ValidationException("密码强度要求无效");
        }
    }

    /**
     * 检查是否允许注册
     */
    public void validateRegisterEnabled() {
        RegisterConfigDTO config = getRegisterConfig();
        if (config != null && !config.isEnableRegister()) {
            throw new ValidationException("注册功能未开启");
        }
    }

    public void validateEmailRegisterEnabled() {
        RegisterConfigDTO config = getRegisterConfig();
        if (config != null && !config.isEnableEmailRegister()) {
            throw new ValidationException("邮箱注册功能未开启");
        }
    }

    public void validateSmsRegisterEnabled() {
        RegisterConfigDTO config = getRegisterConfig();
        if (config != null && !config.isEnableSmsRegister()) {
            throw new ValidationException("短信注册功能未开启");
        }
    }

} 