package com.leyuz.bbs.forum.mybatis;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leyuz.bbs.forum.ForumPO;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author walker
 * @since 2024-08-07
 */
public interface IForumService extends IService<ForumPO> {

    ForumPO getByForumId(Integer forumId);

    ForumPO getByName(String name);

    List<ForumPO> getAllForumShowMenu();

    List<ForumPO> getAllForum();
}
