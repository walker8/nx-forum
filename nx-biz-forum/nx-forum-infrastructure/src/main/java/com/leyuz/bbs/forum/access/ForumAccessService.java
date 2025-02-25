package com.leyuz.bbs.forum.access;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class ForumAccessService extends ServiceImpl<ForumAccessMapper, ForumAccessPO> {

    public List<ForumAccessPO> getByForumId(Integer forumId) {
        return list(new LambdaQueryWrapper<ForumAccessPO>()
                .eq(ForumAccessPO::getForumId, forumId)
                .eq(ForumAccessPO::getIsDeleted, false));
    }

    public boolean batchUpdateForumAccess(List<ForumAccessPO> forumAccessList) {
        if (CollectionUtils.isEmpty(forumAccessList)) {
            return true;
        }
        return updateBatchById(forumAccessList);
    }

    public boolean batchCreateForumAccess(List<ForumAccessPO> forumAccessList) {
        if (CollectionUtils.isEmpty(forumAccessList)) {
            return true;
        }
        return saveBatch(forumAccessList);
    }

    public boolean deleteByForumId(Integer forumId) {
        UpdateWrapper<ForumAccessPO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("forum_id", forumId)
                .set("is_deleted", true);
        return update(null, updateWrapper);
    }
} 