package com.leyuz.bbs.forum.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyuz.bbs.forum.ForumPO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author walker
 * @since 2024-08-07
 */
@Service
public class ForumServiceImpl extends ServiceImpl<ForumMapper, ForumPO> implements IForumService {
    @Override
    public ForumPO getByForumId(Integer forumId) {
        if (forumId == null || forumId <= 0) {
            return null;
        }
        ForumPO forumPO = getById(forumId);
        if (forumPO == null) {
            return null;
        }
        if (Boolean.TRUE.equals(forumPO.getIsDeleted())) {
            return null;
        }
        return forumPO;
    }

    @Override
    public ForumPO getByName(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        QueryWrapper<ForumPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name);
        queryWrapper.eq("is_deleted", false);
        return getOne(queryWrapper);
    }

    @Override
    public List<ForumPO> getAllForumShowMenu() {
        QueryWrapper<ForumPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("show_menu", true);
        queryWrapper.eq("is_deleted", false);
        queryWrapper.orderByAsc("menu_order");
        return list(queryWrapper);
    }

    @Override
    public List<ForumPO> getAllForum() {
        QueryWrapper<ForumPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false);
        queryWrapper.orderByAsc("menu_order");
        return list(queryWrapper);
    }
}
