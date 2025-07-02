package com.leyuz.bbs.report;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.domain.report.ReportE;
import com.leyuz.bbs.domain.report.constant.ReportHandleStatusEnum;
import com.leyuz.bbs.domain.report.constant.ReportTargetTypeEnum;
import com.leyuz.bbs.domain.report.constant.ReportTypeEnum;
import com.leyuz.bbs.domain.report.gateway.ReportGateway;
import com.leyuz.bbs.domain.report.query.ReportPageQuery;
import com.leyuz.bbs.report.database.mapper.ReportMapper;
import com.leyuz.bbs.report.database.po.ReportPO;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.common.mybatis.DataBaseUtils;
import com.leyuz.common.utils.BaseEntityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ReportGatewayImpl implements ReportGateway {

    private final ReportMapper reportMapper;

    @Override
    public boolean save(ReportE report) {
        ReportPO reportPO = toPO(report);
        BaseEntityUtils.setCreateBaseEntity(reportPO);
        return reportMapper.insert(reportPO) > 0;
    }

    @Override
    public ReportE findById(Long reportId) {
        ReportPO reportPO = reportMapper.selectById(reportId);
        return toE(reportPO);
    }

    @Override
    public boolean updateById(ReportE report) {
        ReportPO reportPO = toPO(report);
        BaseEntityUtils.setUpdateBaseEntity(reportPO);
        return reportMapper.updateById(reportPO) > 0;
    }

    @Override
    public CustomPage<ReportE> findPage(ReportPageQuery query) {
        LambdaQueryWrapper<ReportPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Objects.nonNull(query.getTargetType()), ReportPO::getTargetType, query.getTargetType() != null ? query.getTargetType().getValue() : null)
                .eq(Objects.nonNull(query.getForumId()), ReportPO::getForumId, query.getForumId())
                .eq(Objects.nonNull(query.getHandleStatus()), ReportPO::getHandleStatus, query.getHandleStatus() != null ? query.getHandleStatus().getValue() : null)
                .eq(Objects.nonNull(query.getReporterId()), ReportPO::getCreateBy, query.getReporterId())
                .eq(Objects.nonNull(query.getHandlerId()), ReportPO::getUpdateBy, query.getHandlerId())
                .orderByDesc(ReportPO::getCreateTime);

        Page<ReportPO> page = reportMapper.selectPage(new Page<>(query.getPageNo(), query.getPageSize()), wrapper);
        return DataBaseUtils.createCustomPage(page, this::toE);
    }

    @Override
    public long countPendingReports(Integer forumId) {
        LambdaQueryWrapper<ReportPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReportPO::getHandleStatus, ReportHandleStatusEnum.PENDING.getValue());
        if (forumId != null && forumId > 0) {
            wrapper.eq(ReportPO::getForumId, forumId);
        }
        return reportMapper.selectCount(wrapper);
    }

    @Override
    public boolean existsPendingReport(Long userId, Long targetId, ReportTargetTypeEnum targetType) {
        LambdaQueryWrapper<ReportPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReportPO::getCreateBy, userId)
                .eq(ReportPO::getTargetId, targetId)
                .eq(ReportPO::getTargetType, targetType.getValue())
                .eq(ReportPO::getHandleStatus, ReportHandleStatusEnum.PENDING.getValue());
        return reportMapper.selectCount(wrapper) > 0;
    }

    private ReportPO toPO(ReportE report) {
        ReportPO po = new ReportPO();
        po.setReportId(report.getReportId());
        po.setTargetId(report.getTargetId());
        po.setTargetType(report.getTargetType().getValue());
        po.setForumId(report.getForumId());
        po.setReportType(report.getReportType().getValue());
        po.setHandleStatus(report.getHandleStatus().getValue());
        po.setReportReason(report.getReportReason());
        po.setReportedContent(report.getReportedContent());
        po.setHandleReason(report.getHandleReason());
        po.setUserIp(report.getUserIp());
        po.setUserAgent(report.getUserAgent());
        return po;
    }

    private ReportE toE(ReportPO po) {
        ReportE report = new ReportE();
        report.setReportId(po.getReportId());
        report.setReportReason(po.getReportReason());
        report.setTargetId(po.getTargetId());
        report.setTargetType(ReportTargetTypeEnum.of(po.getTargetType()));
        report.setForumId(po.getForumId());
        report.setReportType(ReportTypeEnum.of(po.getReportType()));
        report.setHandleStatus(ReportHandleStatusEnum.of(po.getHandleStatus()));
        report.setHandleReason(po.getHandleReason());
        report.setReportedContent(po.getReportedContent());
        report.setCreateBy(po.getCreateBy());
        report.setUpdateBy(po.getUpdateBy());
        report.setCreateTime(po.getCreateTime());
        report.setUpdateTime(po.getUpdateTime());
        report.setUserIp(po.getUserIp());
        report.setUserAgent(po.getUserAgent());
        report.setReportReason(po.getReportReason());
        return report;
    }
} 