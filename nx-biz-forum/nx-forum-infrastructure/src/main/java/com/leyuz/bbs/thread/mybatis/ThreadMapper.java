package com.leyuz.bbs.thread.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.thread.ThreadPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author walker
 * @since 2024-03-31
 */
@Mapper
public interface ThreadMapper extends BaseMapper<ThreadPO> {
    @Select("SELECT t.* FROM bbs_thread_properties p LEFT JOIN  bbs_threads t on p.thread_id = t.thread_id ${ew.customSqlSegment}")
    List<ThreadPO> selectPropertyList(Page<ThreadPO> page, @Param(Constants.WRAPPER) QueryWrapper<ThreadPO> queryWrapper);

    @Select("SELECT t.* FROM bbs_user_likes l LEFT JOIN  bbs_threads t on l.thread_id = t.thread_id where l.create_by = ${userId} and t.is_deleted = 0 and l.target_type = 0 order by l.id desc")
    List<ThreadPO> selectUserLikeList(Page<ThreadPO> page, Long userId);

    @Select("SELECT t.* FROM bbs_user_favorites f LEFT JOIN  bbs_threads t on f.thread_id = t.thread_id where f.create_by = ${userId} and t.is_deleted = 0 order by f.id desc")
    List<ThreadPO> queryThreadsByUserFavorites(Page<ThreadPO> page, Long userId);
}
