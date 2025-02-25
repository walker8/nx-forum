package com.leyuz.bbs.forum.access;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Mapper
public interface ForumAccessMapper extends BaseMapper<ForumAccessPO> {
    
    default List<ForumAccessPO> getByForumId(Integer forumId) {
        return selectList(new LambdaQueryWrapper<ForumAccessPO>()
                .eq(ForumAccessPO::getForumId, forumId)
                .eq(ForumAccessPO::getIsDeleted, false));
    }
    
    default boolean batchUpdateForumAccess(List<ForumAccessPO> forumAccessList) {
        if (CollectionUtils.isEmpty(forumAccessList)) {
            return true;
        }
        // MyBatis-Plus的批量更新需要逐个更新
        for (ForumAccessPO po : forumAccessList) {
            if (updateById(po) <= 0) {
                return false;
            }
        }
        return true;
    }
    
    default boolean batchCreateForumAccess(List<ForumAccessPO> forumAccessList) {
        if (CollectionUtils.isEmpty(forumAccessList)) {
            return true;
        }
        // MyBatis-Plus的批量插入需要逐个插入
        for (ForumAccessPO po : forumAccessList) {
            if (insert(po) <= 0) {
                return false;
            }
        }
        return true;
    }
    
    default boolean deleteByForumId(Integer forumId) {
        UpdateWrapper<ForumAccessPO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("forum_id", forumId)
                .set("is_deleted", true);
        return update(null, updateWrapper) > 0;
    }
} 