package com.leyuz.bbs.system.config;

import com.alibaba.fastjson2.JSON;
import com.leyuz.bbs.system.config.dto.AuditConfigBlackWhiteUsersDTO;
import com.leyuz.bbs.system.config.dto.AuditConfigSensitiveWordsDTO;
import com.leyuz.bbs.system.config.dto.ConfigConst;
import com.leyuz.common.exception.ValidationException;
import com.leyuz.module.config.app.ConfigApplication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditConfigApplication {
    private final ConfigApplication configApplication;

    /**
     * 获取敏感词审核项
     *
     * @return
     */
    public AuditConfigSensitiveWordsDTO getAuditConfigSensitiveWords() {
        AuditConfigSensitiveWordsDTO config = configApplication.getConfigValueByKey(ConfigConst.AUDIT_CONFIG_SENSITIVE_WORDS, AuditConfigSensitiveWordsDTO.class);
        if (config == null) {
            config = new AuditConfigSensitiveWordsDTO();
            updateAuditConfigSensitiveWords(config);
        }
        return config;
    }

    /**
     * 更新敏感词审核项
     *
     * @param config
     */
    public void updateAuditConfigSensitiveWords(AuditConfigSensitiveWordsDTO config) {
        if (config == null) {
            throw new ValidationException("审核配置不能为空");
        }
        if (config.getSensitiveWords() == null) {
            config.setSensitiveWords(new HashSet<>());
        }
        configApplication.updateConfig(ConfigConst.AUDIT_CONFIG_SENSITIVE_WORDS, JSON.toJSONString(config));
    }

    /**
     * 获取黑白名单审核项
     *
     * @return
     */
    public AuditConfigBlackWhiteUsersDTO getAuditConfigBlackWhiteUsers() {
        AuditConfigBlackWhiteUsersDTO config = configApplication.getConfigValueByKey(ConfigConst.AUDIT_CONFIG_BLACK_WHITE_USERS, AuditConfigBlackWhiteUsersDTO.class);
        if (config == null) {
            config = new AuditConfigBlackWhiteUsersDTO();
            updateAuditConfigBlackWhiteUsers(config);
        }
        return config;
    }

    /**
     * 更新黑白名单审核项
     *
     * @param config
     */
    public void updateAuditConfigBlackWhiteUsers(AuditConfigBlackWhiteUsersDTO config) {
        if (config == null) {
            throw new ValidationException("审核配置不能为空");
        }
        if (config.getBlackListUsers() == null) {
            config.setBlackListUsers(new HashSet<>());
        }
        if (config.getWhiteListUsers() == null) {
            config.setWhiteListUsers(new HashSet<>());
        }
        configApplication.updateConfig(ConfigConst.AUDIT_CONFIG_BLACK_WHITE_USERS, JSON.toJSONString(config));
    }
} 