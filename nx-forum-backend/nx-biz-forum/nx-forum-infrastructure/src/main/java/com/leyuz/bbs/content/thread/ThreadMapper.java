package com.leyuz.bbs.content.thread;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.content.thread.dataobject.ThreadPropertyAttributeTopV;
import com.leyuz.bbs.content.thread.dataobject.ThreadPropertyTypeV;
import com.leyuz.common.mybatis.PageQuery;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 帖子 Mapper 接口
 * </p>
 *
 * @author walker
 * @since 2024-03-31
 */
@Mapper
public interface ThreadMapper extends BaseMapper<ThreadPO> {
    @Select("SELECT DISTINCT t.* FROM bbs_thread_properties p LEFT JOIN  bbs_threads t on p.thread_id = t.thread_id ${ew.customSqlSegment}")
    List<ThreadPO> selectPropertyList(Page<ThreadPO> page, @Param(Constants.WRAPPER) QueryWrapper<ThreadPO> queryWrapper);

    @Select("SELECT t.* FROM bbs_user_likes l LEFT JOIN  bbs_threads t on l.thread_id = t.thread_id where l.create_by = ${userId} and t.is_deleted = 0 and l.target_type = 0 order by l.id desc")
    List<ThreadPO> selectUserLikeList(Page<ThreadPO> page, Long userId);

    @Select("SELECT t.* FROM bbs_user_favorites f LEFT JOIN  bbs_threads t on f.thread_id = t.thread_id where f.create_by = ${userId} and t.is_deleted = 0 order by f.id desc")
    List<ThreadPO> queryThreadsByUserFavorites(Page<ThreadPO> page, Long userId);

    /**
     * 分页查询帖子
     *
     * @param forumId   版块ID
     * @param pageQuery 分页查询条件
     * @return 帖子分页结果
     */
    default Page<ThreadPO> queryThreads(Integer forumId, PageQuery pageQuery) {
        Map<String, Object> params = pageQuery.getParams();
        String tableAlias;
        Integer propertyType = MapUtil.getInt(params, "propertyType");
        if (propertyType != null) {
            tableAlias = "t.";
        } else {
            tableAlias = "";
        }
        QueryWrapper<ThreadPO> queryWrapper = pageQuery.toQueryWrapper(tableAlias);
        if (forumId != null && forumId > 0) {
            queryWrapper.eq(tableAlias + "forum_id", forumId);
        } else {
            // 全局查询时排除归档版块(forum_id=6)的主题
            queryWrapper.ne(tableAlias + "forum_id", 6);
        }
        String keyword = MapUtil.getStr(params, "keyword");
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> wrapper.like(tableAlias + "subject", keyword)
                    .or().like(tableAlias + "brief", keyword));
        }
        String ip = MapUtil.getStr(params, "ip");
        if (StringUtils.isNotBlank(ip)) {
            queryWrapper.eq(tableAlias + "user_ip", ip);
        }
        Long userId = MapUtil.getLong(params, "userId");
        if (userId != null) {
            queryWrapper.eq(tableAlias + "create_by", userId);
        }
        Integer auditStatus = MapUtil.getInt(params, "auditStatus");
        if (auditStatus != null) {
            queryWrapper.eq(tableAlias + "audit_status", auditStatus);
        }
        Integer days = MapUtil.getInt(params, "days");
        if (days != null && days > 0) {
            // 查询days天内的帖子，即创建时间大于等于days天前的时间点
            queryWrapper.ge(tableAlias + "create_time", DateUtil.offsetDay(new Date(), -days));
        }
        Map<String, Object> excludeThreadMap = MapUtil.getAny(params, "excludeThreadIds");
        if (!excludeThreadMap.isEmpty()) {
            List<Long> excludeThreadIds = (List<Long>) excludeThreadMap.get("excludeThreadIds");
            queryWrapper.notIn(tableAlias + "thread_id", excludeThreadIds);
        }
        if (propertyType == null) {
            // 分页参数
            return selectPage(pageQuery.toPage(), queryWrapper);
        } else {
            queryWrapper.eq("p.property_type", propertyType);
            Page<ThreadPO> page = pageQuery.toPage();
            List<ThreadPO> threadPOList = selectPropertyList(page, queryWrapper);
            page.setRecords(threadPOList);
            return page;
        }
    }

    /**
     * 查询置顶帖子
     *
     * @param forumId 版块ID
     * @return 置顶帖子列表
     */
    default List<ThreadPO> queryTopThreads(Integer forumId) {
        Page<ThreadPO> page = new Page<>(1, 5);
        QueryWrapper<ThreadPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper -> {
            wrapper.eq("p.attribute", ThreadPropertyAttributeTopV.GLOBAL.getValue());
            if (forumId != null && forumId > 0) {
                wrapper.or().apply("(p.forum_id = {0} AND p.attribute = {1})",
                        forumId,
                        ThreadPropertyAttributeTopV.CURRENT_FORUM.getValue());
            }
        });
        queryWrapper.eq("p.property_type", ThreadPropertyTypeV.TOP.getValue());
        queryWrapper.eq("t.is_deleted", Boolean.FALSE);
        return selectPropertyList(page, queryWrapper);
    }

    /**
     * 更新帖子浏览次数
     *
     * @param threadId  帖子ID
     * @param increment 增量
     */
    default void updateViews(Long threadId, int increment) {
        UpdateWrapper<ThreadPO> wrapper = new UpdateWrapper<ThreadPO>()
                .setSql("views = views + " + increment)
                .eq("thread_id", threadId)
                .eq("is_deleted", false);
        update(null, wrapper);
    }

    /**
     * 获取帖子总数
     *
     * @param forumId 版块ID
     * @return 帖子总数
     */
    default Long getThreadCount(Integer forumId) {
        QueryWrapper<ThreadPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false);
        if (forumId != null && forumId > 0) {
            queryWrapper.eq("forum_id", forumId);
        }
        return selectCount(queryWrapper);
    }

    /**
     * 根据用户ID列表查询帖子
     *
     * @param userIds  用户ID列表
     * @param pageNo   页码
     * @param pageSize 每页大小
     * @return 帖子分页结果
     */
    default Page<ThreadPO> queryThreadsByUserIds(List<Long> userIds, Integer pageNo, Integer pageSize) {
        Page<ThreadPO> page = new Page<>(pageNo, pageSize);
        QueryWrapper<ThreadPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("create_by", userIds);
        queryWrapper.eq("is_deleted", false);
        queryWrapper.eq("audit_status", 0); // 只查询已通过审核的帖子
        queryWrapper.orderByDesc("create_time"); // 按创建时间倒序排序
        return selectPage(page, queryWrapper);
    }
}
