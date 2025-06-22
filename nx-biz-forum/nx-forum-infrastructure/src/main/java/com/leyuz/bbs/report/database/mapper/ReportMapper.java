package com.leyuz.bbs.report.database.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyuz.bbs.report.database.po.ReportPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 举报Mapper
 */
@Mapper
public interface ReportMapper extends BaseMapper<ReportPO> {
} 