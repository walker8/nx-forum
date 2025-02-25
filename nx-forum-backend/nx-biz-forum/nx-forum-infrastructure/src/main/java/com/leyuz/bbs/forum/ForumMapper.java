package com.leyuz.bbs.forum;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyuz.bbs.forum.ForumPO;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author walker
 * @since 2024-08-07
 */
@Mapper
public interface ForumMapper extends BaseMapper<ForumPO> {
    
    default ForumPO getByForumId(Integer forumId) {
        if (forumId == null || forumId <= 0) {
            return null;
        }
        ForumPO forumPO = selectById(forumId);
        if (forumPO == null) {
            return null;
        }
        if (Boolean.TRUE.equals(forumPO.getIsDeleted())) {
            return null;
        }
        return forumPO;
    }
    
    default ForumPO getByName(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        QueryWrapper<ForumPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name);
        queryWrapper.eq("is_deleted", false);
        return selectOne(queryWrapper);
    }
    
    default List<ForumPO> getAllForumShowMenu() {
        QueryWrapper<ForumPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("show_menu", true);
        queryWrapper.eq("is_deleted", false);
        queryWrapper.orderByAsc("menu_order");
        return selectList(queryWrapper);
    }
    
    default List<ForumPO> getAllForum() {
        QueryWrapper<ForumPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false);
        queryWrapper.orderByAsc("menu_order");
        return selectList(queryWrapper);
    }
}
