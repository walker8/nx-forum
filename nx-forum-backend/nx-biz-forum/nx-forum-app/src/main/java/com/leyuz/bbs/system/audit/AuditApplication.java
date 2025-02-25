package com.leyuz.bbs.system.audit;

import cn.hutool.core.lang.Pair;
import com.leyuz.bbs.system.audit.dto.AuditConfigBlackWhiteUsersVO;
import com.leyuz.bbs.system.audit.dto.AuditDTO;
import com.leyuz.bbs.common.dataobject.AuditStatusV;
import com.leyuz.bbs.system.config.AuditConfigApplication;
import com.leyuz.bbs.system.config.dto.AuditConfigBlackWhiteUsersDTO;
import com.leyuz.bbs.system.config.dto.AuditConfigSensitiveWordsDTO;
import com.leyuz.uc.user.UserApplication;
import com.leyuz.uc.user.dto.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditApplication {
    private final UserApplication userApplication;
    private final AuditConfigApplication auditConfigApplication;

    public AuditConfigBlackWhiteUsersVO getAuditConfigBlackWhiteUsersVO() {
        AuditConfigBlackWhiteUsersDTO auditConfigBlackWhiteUsers = auditConfigApplication.getAuditConfigBlackWhiteUsers();
        AuditConfigBlackWhiteUsersVO auditConfigBlackWhiteUsersVO = new AuditConfigBlackWhiteUsersVO();
        auditConfigBlackWhiteUsers.getWhiteListUsers().forEach(userId -> {
            try {
                UserVO userVO = userApplication.getUserInfo(userId);
                auditConfigBlackWhiteUsersVO.getWhiteListUsers().add(userVO);
            } catch (Exception e) {
                log.error("获取用户信息失败，userId = {}", userId);
            }
        });
        auditConfigBlackWhiteUsers.getBlackListUsers().forEach(userId -> {
            try {
                UserVO userVO = userApplication.getUserInfo(userId);
                auditConfigBlackWhiteUsersVO.getBlackListUsers().add(userVO);
            } catch (Exception e) {
                log.error("获取用户信息失败，userId = {}", userId);
            }
        });
        return auditConfigBlackWhiteUsersVO;
    }

    // 检查文本是否包含敏感词，返回包含的敏感词
    private String hasSensitiveWords(String text) {
        AuditConfigSensitiveWordsDTO config = auditConfigApplication.getAuditConfigSensitiveWords();
        if (!config.isEnableSensitiveWordsAudit()) {
            return "";
        }

        String lowerText = text.toLowerCase();
        return config.getSensitiveWords().stream()
                .filter(word -> lowerText.contains(word.toLowerCase()))
                .findFirst().orElse("");
    }

    /**
     * 审核策略
     *
     * @param auditDTO
     * @return
     */
    public Pair<AuditStatusV, String> check(AuditDTO auditDTO) {
        // 黑白名单检查
        AuditConfigBlackWhiteUsersDTO config = auditConfigApplication.getAuditConfigBlackWhiteUsers();
        if (config.getWhiteListUsers().contains(auditDTO.getUserId())) {
            return Pair.of(AuditStatusV.PASSED, "");
        }
        if (config.getBlackListUsers().contains(auditDTO.getUserId())) {
            return Pair.of(AuditStatusV.AUDITING, "黑名单审核");
        }

        // 敏感词检查
        String sensitiveWord = hasSensitiveWords(auditDTO.getMessage());
        if (StringUtils.isNotEmpty(sensitiveWord)) {
            return Pair.of(AuditStatusV.AUDITING, "包含敏感词：" + sensitiveWord);
        }

        return Pair.of(AuditStatusV.PASSED, "");
    }
}
