package com.leyuz.bbs.system.audit;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 审核记录 Mapper
 * </p>
 *
 * @author walker
 * @since 2026-08-19
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLogPO> {
}
