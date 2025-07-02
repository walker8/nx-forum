package com.leyuz.bbs.domain.report.gateway;

import com.leyuz.bbs.domain.report.ReportE;
import com.leyuz.bbs.domain.report.constant.ReportTargetTypeEnum;
import com.leyuz.bbs.domain.report.query.ReportPageQuery;
import com.leyuz.common.mybatis.CustomPage;

/**
 * 举报网关接口
 */
public interface ReportGateway {

    /**
     * 保存举报信息
     *
     * @param report 举报实体
     * @return boolean
     */
    boolean save(ReportE report);

    /**
     * 根据ID查询举报信息
     *
     * @param reportId 举报ID
     * @return ReportE
     */
    ReportE findById(Long reportId);

    /**
     * 更新举报信息
     *
     * @param report 举报实体
     * @return boolean
     */
    boolean updateById(ReportE report);

    /**
     * 分页查询举报列表
     *
     * @param query 查询条件
     * @return CustomPage<ReportE>
     */
    CustomPage<ReportE> findPage(ReportPageQuery query);

    /**
     * 统计待处理的举报数量
     *
     * @param forumId 版块ID
     * @return long
     */
    long countPendingReports(Integer forumId);

    /**
     * 判断是否存在待处理的重复举报
     *
     * @param userId     举报人ID
     * @param targetId   被举报内容ID
     * @param targetType 举报目标类型
     * @return 是否存在
     */
    boolean existsPendingReport(Long userId, Long targetId, ReportTargetTypeEnum targetType);
} 