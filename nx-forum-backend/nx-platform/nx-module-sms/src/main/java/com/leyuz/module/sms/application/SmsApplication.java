package com.leyuz.module.sms.application;

import com.alibaba.fastjson2.JSON;
import com.leyuz.common.exception.ValidationException;
import com.leyuz.module.config.app.ConfigApplication;
import com.leyuz.module.sms.dto.SmsConfigDTO;
import com.leyuz.module.sms.infrastructure.AliyunSmsClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmsApplication {

    private final ConfigApplication configApplication;
    private final AliyunSmsClient aliyunSmsClient;
    private static final String SMS_CONFIG_KEY = "sms_config";

    public void sendVerifyCode(String phone) {
        // 获取短信配置
        SmsConfigDTO config = getSmsConfig();
        if (config == null || !config.getEnabled()) {
            throw new ValidationException("短信服务未启用");
        }

        // 检查黑名单
        if (config.getBlacklist() != null && config.getBlacklist().contains(phone)) {
            throw new ValidationException("该手机号已被列入黑名单");
        }

        // 生成6位随机验证码
        String code = generateVerifyCode();

        // 根据不同供应商实现发送逻辑
        switch (config.getProvider().toUpperCase()) {
            case "ALIYUN":
                aliyunSmsClient.sendSms(phone, code, config);
                break;
            default:
                throw new ValidationException("不支持的短信服务提供商");
        }
    }

    private String generateVerifyCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }

    public void updateSmsConfig(SmsConfigDTO config) {
        configApplication.updateConfig(SMS_CONFIG_KEY, JSON.toJSONString(config));
    }

    public SmsConfigDTO getSmsConfig() {
        return configApplication.getConfigValueByKey(SMS_CONFIG_KEY, SmsConfigDTO.class);
    }
}