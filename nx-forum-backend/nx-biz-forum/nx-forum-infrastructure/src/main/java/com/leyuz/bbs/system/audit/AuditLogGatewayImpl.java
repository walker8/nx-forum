package com.leyuz.bbs.system.audit;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.common.dataobject.AuditStageV;
import com.leyuz.bbs.common.dataobject.AuditStatusV;
import com.leyuz.bbs.common.dataobject.AuditTriggerSourceV;
import com.leyuz.bbs.common.dataobject.OperatorTypeV;
import com.leyuz.bbs.system.audit.domain.AuditLogE;
import com.leyuz.bbs.system.audit.domain.gateway.AuditLogGateway;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.common.utils.BaseEntityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 审核记录仓储实现
 */
@Repository
@RequiredArgsConstructor
public class AuditLogGatewayImpl implements AuditLogGateway {

    private final AuditLogMapper auditLogMapper;

    @Override
    public void save(AuditLogE auditLogE) {
        if (auditLogE == null) {
            return;
        }
        AuditLogPO po = new AuditLogPO();
        BeanUtil.copyProperties(auditLogE, po);
        // 枚举转 byte
        po.setContentType(auditLogE.getContentType() == null ? null : auditLogE.getContentType().byteValue());
        po.setTriggerSource(auditLogE.getTriggerSource() == null ? null : (byte) auditLogE.getTriggerSource().getValue());
        po.setAuditStage(auditLogE.getAuditStage() == null ? null : (byte) auditLogE.getAuditStage().getValue());
        po.setAuditStatusBefore(auditLogE.getAuditStatusBefore() == null ? null : (byte) auditLogE.getAuditStatusBefore().getValue());
        po.setAuditStatusAfter(auditLogE.getAuditStatusAfter() == null ? null : (byte) auditLogE.getAuditStatusAfter().getValue());
        po.setOperatorType(auditLogE.getOperatorType() == null ? null : (byte) auditLogE.getOperatorType().getValue());
        BaseEntityUtils.setCreateBaseEntity(po);
        auditLogMapper.insert(po);
        auditLogE.setLogId(po.getLogId());
        auditLogE.setCreateTime(po.getCreateTime());
    }

    @Override
    public AuditLogE getById(Long logId) {
        if (logId == null) {
            return null;
        }
        AuditLogPO po = auditLogMapper.selectById(logId);
        return convert(po);
    }

    @Override
    public List<AuditLogE> listByContent(Integer contentType, Long contentId) {
        if (contentType == null || contentId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<AuditLogPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLogPO::getContentType, contentType.byteValue())
                .eq(AuditLogPO::getContentId, contentId)
                .eq(AuditLogPO::getIsDeleted, false)
                .orderByDesc(AuditLogPO::getCreateTime);
        return auditLogMapper.selectList(wrapper).stream()
                .map(this::convert)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public CustomPage<AuditLogE> pageAuditLogs(Integer contentType, Long contentId, Long userId,
                                               Integer forumId, Byte auditStage, Byte triggerSource,
                                               Byte operatorType, Byte auditStatusAfter,
                                               int pageNo, int pageSize) {
        LambdaQueryWrapper<AuditLogPO> wrapper = buildWrapper(contentType, contentId, userId,
                forumId, auditStage, triggerSource, operatorType, auditStatusAfter);
        Page<AuditLogPO> page = auditLogMapper.selectPage(Page.of(pageNo, pageSize), wrapper);
        List<AuditLogE> records = page.getRecords().stream()
                .map(this::convert)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return CustomPage.<AuditLogE>builder()
                .records(records)
                .total(page.getTotal())
                .size(page.getSize())
                .current(page.getCurrent())
                .hasNext(page.getCurrent() < page.getPages())
                .build();
    }

    /**
     * 构建分页查询条件
     */
    private LambdaQueryWrapper<AuditLogPO> buildWrapper(Integer contentType, Long contentId, Long userId,
                                                        Integer forumId, Byte auditStage, Byte triggerSource,
                                                        Byte operatorType, Byte auditStatusAfter) {
        LambdaQueryWrapper<AuditLogPO> wrapper = new LambdaQueryWrapper<>();
        if (contentType != null) {
            wrapper.eq(AuditLogPO::getContentType, contentType.byteValue());
        }
        if (contentId != null) {
            wrapper.eq(AuditLogPO::getContentId, contentId);
        }
        if (userId != null) {
            wrapper.eq(AuditLogPO::getUserId, userId);
        }
        if (forumId != null) {
            wrapper.eq(AuditLogPO::getForumId, forumId);
        }
        if (auditStage != null) {
            wrapper.eq(AuditLogPO::getAuditStage, auditStage);
        }
        if (triggerSource != null) {
            wrapper.eq(AuditLogPO::getTriggerSource, triggerSource);
        }
        if (operatorType != null) {
            wrapper.eq(AuditLogPO::getOperatorType, operatorType);
        }
        if (auditStatusAfter != null) {
            wrapper.eq(AuditLogPO::getAuditStatusAfter, auditStatusAfter);
        }
        wrapper.eq(AuditLogPO::getIsDeleted, false)
                .orderByDesc(AuditLogPO::getCreateTime);
        return wrapper;
    }

    @Override
    public int updateContentIdBySessionId(String sessionId, Long contentId) {
        if (sessionId == null || sessionId.isEmpty() || contentId == null) {
            return 0;
        }
        LambdaUpdateWrapper<AuditLogPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AuditLogPO::getAuditSessionId, sessionId)
                .isNull(AuditLogPO::getContentId)
                .eq(AuditLogPO::getIsDeleted, false)
                .set(AuditLogPO::getContentId, contentId)
                .set(AuditLogPO::getUpdateBy, com.leyuz.common.utils.HeaderUtils.getUserId())
                .set(AuditLogPO::getUpdateTime, java.time.LocalDateTime.now());
        return auditLogMapper.update(null, wrapper);
    }

    /**
     * PO 转 Entity
     */
    private AuditLogE convert(AuditLogPO po) {
        if (po == null) {
            return null;
        }
        AuditLogE e = new AuditLogE();
        BeanUtil.copyProperties(po, e);
        e.setContentType(po.getContentType() == null ? null : po.getContentType().intValue());
        e.setTriggerSource(AuditTriggerSourceV.of(po.getTriggerSource()));
        e.setAuditStage(AuditStageV.of(po.getAuditStage()));
        e.setAuditStatusBefore(AuditStatusV.of(po.getAuditStatusBefore()));
        e.setAuditStatusAfter(AuditStatusV.of(po.getAuditStatusAfter()));
        e.setOperatorType(OperatorTypeV.of(po.getOperatorType()));
        return e;
    }
}
