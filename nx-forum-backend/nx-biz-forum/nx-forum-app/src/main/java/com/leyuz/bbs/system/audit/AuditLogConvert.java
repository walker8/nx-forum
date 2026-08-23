package com.leyuz.bbs.system.audit;

import cn.hutool.core.bean.BeanUtil;
import com.leyuz.bbs.common.dataobject.AuditStageV;
import com.leyuz.bbs.common.dataobject.AuditStatusV;
import com.leyuz.bbs.common.dataobject.AuditTriggerSourceV;
import com.leyuz.bbs.common.dataobject.OperatorTypeV;
import com.leyuz.bbs.forum.ForumApplication;
import com.leyuz.bbs.forum.ForumPO;
import com.leyuz.bbs.system.audit.domain.AuditLogE;
import com.leyuz.bbs.system.audit.dto.AuditLogVO;
import com.leyuz.common.ip.AddressUtils;
import com.leyuz.uc.user.UserApplication;
import com.leyuz.uc.user.UserE;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 审核记录转换器：Entity → VO（含用户名/版块名/IP 地址等展示字段）
 */
@Component
@RequiredArgsConstructor
public class AuditLogConvert {

    private static final Map<Integer, String> CONTENT_TYPE_MAP = new HashMap<>();
    private static final Map<AuditTriggerSourceV, String> TRIGGER_SOURCE_MAP = new HashMap<>();
    private static final Map<AuditStageV, String> AUDIT_STAGE_MAP = new HashMap<>();
    private static final Map<AuditStatusV, String> AUDIT_STATUS_MAP = new HashMap<>();
    private static final Map<OperatorTypeV, String> OPERATOR_TYPE_MAP = new HashMap<>();

    static {
        CONTENT_TYPE_MAP.put(1, "主贴");
        CONTENT_TYPE_MAP.put(2, "评论");
        CONTENT_TYPE_MAP.put(3, "楼中楼");

        TRIGGER_SOURCE_MAP.put(AuditTriggerSourceV.NEW, "主动发布");
        TRIGGER_SOURCE_MAP.put(AuditTriggerSourceV.EDIT, "编辑触发");
        TRIGGER_SOURCE_MAP.put(AuditTriggerSourceV.REPORT, "举报触发");
        TRIGGER_SOURCE_MAP.put(AuditTriggerSourceV.MANUAL, "管理员后台");

        AUDIT_STAGE_MAP.put(AuditStageV.BLACK_WHITE, "黑白名单");
        AUDIT_STAGE_MAP.put(AuditStageV.SENSITIVE_WORD, "敏感词");
        AUDIT_STAGE_MAP.put(AuditStageV.RULE_ENGINE, "规则引擎");
        AUDIT_STAGE_MAP.put(AuditStageV.AI, "AI 审核");
        AUDIT_STAGE_MAP.put(AuditStageV.MANUAL, "人工复审");

        AUDIT_STATUS_MAP.put(AuditStatusV.PASSED, "通过");
        AUDIT_STATUS_MAP.put(AuditStatusV.AUDITING, "审核中");
        AUDIT_STATUS_MAP.put(AuditStatusV.REJECTED, "拒绝");

        OPERATOR_TYPE_MAP.put(OperatorTypeV.SYSTEM, "系统");
        OPERATOR_TYPE_MAP.put(OperatorTypeV.ADMIN, "管理员");
    }

    private final UserApplication userApplication;
    private final ForumApplication forumApplication;

    /**
     * Entity → VO
     */
    public AuditLogVO convertToVO(AuditLogE e) {
        if (e == null) {
            return null;
        }
        AuditLogVO vo = new AuditLogVO();
        BeanUtil.copyProperties(e, vo);

        // 枚举 byte → 数值（前端按数值匹配 tag 颜色）
        vo.setContentType(e.getContentType());
        vo.setTriggerSource(e.getTriggerSource() == null ? null : (byte) e.getTriggerSource().getValue());
        vo.setAuditStage(e.getAuditStage() == null ? null : (byte) e.getAuditStage().getValue());
        vo.setAuditStatusBefore(e.getAuditStatusBefore() == null ? null : (byte) e.getAuditStatusBefore().getValue());
        vo.setAuditStatusAfter(e.getAuditStatusAfter() == null ? null : (byte) e.getAuditStatusAfter().getValue());
        vo.setOperatorType(e.getOperatorType() == null ? null : (byte) e.getOperatorType().getValue());

        // 中文名称
        vo.setContentTypeName(CONTENT_TYPE_MAP.getOrDefault(e.getContentType(), ""));
        vo.setTriggerSourceName(e.getTriggerSource() == null ? "" : TRIGGER_SOURCE_MAP.getOrDefault(e.getTriggerSource(), ""));
        vo.setAuditStageName(e.getAuditStage() == null ? "" : AUDIT_STAGE_MAP.getOrDefault(e.getAuditStage(), ""));
        vo.setAuditStatusAfterName(e.getAuditStatusAfter() == null ? "" : AUDIT_STATUS_MAP.getOrDefault(e.getAuditStatusAfter(), ""));
        vo.setOperatorTypeName(e.getOperatorType() == null ? "" : OPERATOR_TYPE_MAP.getOrDefault(e.getOperatorType(), ""));

        // 作者用户名
        if (e.getUserId() != null) {
            try {
                UserE author = userApplication.getByIdFromCache(e.getUserId());
                vo.setAuthorName(author == null ? "用户不存在" : author.getUserName());
            } catch (Exception ex) {
                vo.setAuthorName("");
            }
        }

        // 版块名
        if (e.getForumId() != null) {
            try {
                ForumPO forum = forumApplication.getForumById(e.getForumId());
                vo.setForumName(forum == null ? "" : forum.getNickName());
            } catch (Exception ex) {
                vo.setForumName("");
            }
        }

        // 操作人用户名
        if (e.getOperatorId() != null) {
            try {
                UserE operator = userApplication.getByIdFromCache(e.getOperatorId());
                vo.setOperatorName(operator == null ? "用户不存在" : operator.getUserName());
            } catch (Exception ex) {
                vo.setOperatorName("");
            }
        }

        // IP 地理位置
        if (e.getUserIp() != null && !e.getUserIp().isEmpty()) {
            try {
                vo.setIpLocation(AddressUtils.getCityByIP(e.getUserIp()));
            } catch (Exception ex) {
                vo.setIpLocation("");
            }
        }

        return vo;
    }

    /**
     * 批量转换
     */
    public List<AuditLogVO> convertToVOs(List<AuditLogE> list) {
        if (list == null || list.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }
}
