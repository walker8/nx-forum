package com.leyuz.bbs.user.ban;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.common.mybatis.PageQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

/**
 * 禁言记录表 Mapper 接口
 */
@Mapper
public interface BanMapper extends BaseMapper<BanPO> {

    /**
     * 分页查询禁言记录
     *
     * @param forumId   版块ID
     * @param pageQuery 分页查询条件
     * @return 禁言记录分页结果
     */
    default Page<BanPO> queryBans(Integer forumId, PageQuery pageQuery) {
        QueryWrapper<BanPO> queryWrapper = pageQuery.toQueryWrapper();
        if (forumId != null && forumId > 0) {
            queryWrapper.eq("forum_id", forumId);
        }
        Map<String, Object> params = pageQuery.getParams();
        Long userId = MapUtil.getLong(params, "userId");
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }
        // 分页参数
        return selectPage(pageQuery.toPage(), queryWrapper);
    }
} 