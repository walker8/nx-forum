package com.leyuz.bbs.user.mybatis;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyuz.bbs.user.ForumUserPropertyDO;
import com.leyuz.common.utils.BaseEntityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ForumUserPropertyServiceImpl extends ServiceImpl<ForumUserPropertyMapper, ForumUserPropertyDO> implements ForumUserPropertyService {

    @Override
    public ForumUserPropertyDO getByUserId(Long userId) {
        return lambdaQuery().eq(ForumUserPropertyDO::getUserId, userId)
                .eq(ForumUserPropertyDO::getIsDeleted, false)
                .one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initUserProperty(Long userId) {
        // 检查是否已存在
        if (getByUserId(userId) != null) {
            return;
        }

        createUserProperty(userId);
    }

    private ForumUserPropertyDO createUserProperty(Long userId) {
        ForumUserPropertyDO property = new ForumUserPropertyDO();
        property.setUserId(userId);
        property.setThreads(0);
        property.setComments(0);
        property.setFans(0);
        property.setCredits(0);
        property.setGolds(0);
        property.setIsDeleted(false);
        BaseEntityUtils.setCreateBaseEntity(property);

        save(property);

        return property;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementThreads(Long userId) {
        updateProperty(userId, property -> property.setThreads(property.getThreads() + 1));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementThreads(Long userId) {
        updateProperty(userId, property -> {
            if (property.getThreads() > 0) {
                property.setThreads(property.getThreads() - 1);
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementComments(Long userId) {
        updateProperty(userId, property -> property.setComments(property.getComments() + 1));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementComments(Long userId) {
        updateProperty(userId, property -> {
            if (property.getComments() > 0) {
                property.setComments(property.getComments() - 1);
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementFans(Long userId) {
        updateProperty(userId, property -> property.setFans(property.getFans() + 1));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementFans(Long userId) {
        updateProperty(userId, property -> {
            if (property.getFans() > 0) {
                property.setFans(property.getFans() - 1);
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCredits(Long userId, int amount) {
        updateProperty(userId, property -> property.setCredits(property.getCredits() + amount));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addGolds(Long userId, int amount) {
        updateProperty(userId, property -> property.setGolds(property.getGolds() + amount));
    }

    private void updateProperty(Long userId, PropertyUpdater updater) {
        ForumUserPropertyDO property = getByUserId(userId);
        if (property == null) {
            property = createUserProperty(userId);
        }

        // 更新属性
        updater.update(property);
        BaseEntityUtils.setUpdateBaseEntity(property);

        // 保存更新
        updateById(property);
    }

    @FunctionalInterface
    private interface PropertyUpdater {
        void update(ForumUserPropertyDO property);
    }
} 