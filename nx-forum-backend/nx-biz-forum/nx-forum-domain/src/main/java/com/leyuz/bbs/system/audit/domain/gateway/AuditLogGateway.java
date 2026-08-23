package com.leyuz.bbs.system.audit.domain.gateway;

import com.leyuz.bbs.system.audit.domain.AuditLogE;
import com.leyuz.common.mybatis.CustomPage;

import java.util.List;

/**
 * 审核记录仓储接口
 */
public interface AuditLogGateway {

    /**
     * 保存审核记录
     *
     * @param auditLogE 审核记录实体
     */
    void save(AuditLogE auditLogE);

    /**
     * 根据日志ID查询
     *
     * @param logId 日志ID
     * @return 审核记录实体，未找到返回 null
     */
    AuditLogE getById(Long logId);

    /**
     * 查询某内容的所有审核记录（按时间倒序）
     *
     * @param contentType 内容类型
     * @param contentId   内容ID
     * @return 审核记录列表
     */
    List<AuditLogE> listByContent(Integer contentType, Long contentId);

    /**
     * 分页查询审核记录
     *
     * @param contentType      内容类型（可空）
     * @param contentId        内容ID（可空）
     * @param userId           作者ID（可空）
     * @param forumId          版块ID（可空）
     * @param auditStage       审核阶段（可空）
     * @param triggerSource    触发来源（可空）
     * @param operatorType     操作人类型（可空）
     * @param auditStatusAfter 审核后状态（可空）
     * @param pageNo           页码（从 1 开始）
     * @param pageSize         每页大小
     * @return 分页审核记录
     */
    CustomPage<AuditLogE> pageAuditLogs(Integer contentType, Long contentId, Long userId,
                                        Integer forumId, Byte auditStage, Byte triggerSource,
                                        Byte operatorType, Byte auditStatusAfter,
                                        int pageNo, int pageSize);

    /**
     * 回填指定会话下、contentId 仍为 NULL 的审核记录的 contentId
     *
     * <p>同步阶段（黑白名单/敏感词/规则引擎）在内容入库前写日志时无法拿到 contentId，
     * 内容入库后通过本方法按 sessionId 批量回填，避免列表展示「未保存」。
     *
     * @param sessionId 审核会话ID
     * @param contentId 内容入库后获得的主键
     * @return 受影响行数
     */
    int updateContentIdBySessionId(String sessionId, Long contentId);
}
