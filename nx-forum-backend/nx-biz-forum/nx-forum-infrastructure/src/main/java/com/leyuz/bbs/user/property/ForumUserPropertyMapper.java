package com.leyuz.bbs.user.property;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyuz.bbs.user.property.ForumUserPropertyPO;
import com.leyuz.common.utils.BaseEntityUtils;
import org.apache.ibatis.annotations.Mapper;

import java.util.function.Consumer;

@Mapper
public interface ForumUserPropertyMapper extends BaseMapper<ForumUserPropertyPO> {
    
    default ForumUserPropertyPO getByUserId(Long userId) {
        return selectOne(new LambdaQueryWrapper<ForumUserPropertyPO>()
                .eq(ForumUserPropertyPO::getUserId, userId)
                .eq(ForumUserPropertyPO::getIsDeleted, false));
    }
    
    default void initUserProperty(Long userId) {
        // 检查是否已存在
        if (getByUserId(userId) != null) {
            return;
        }
        createUserProperty(userId);
    }
    
    default ForumUserPropertyPO createUserProperty(Long userId) {
        ForumUserPropertyPO property = new ForumUserPropertyPO();
        property.setUserId(userId);
        property.setThreads(0);
        property.setComments(0);
        property.setFans(0);
        property.setCredits(0);
        property.setGolds(0);
        property.setIsDeleted(false);
        BaseEntityUtils.setCreateBaseEntity(property);
        insert(property);
        return property;
    }
    
    default void incrementThreads(Long userId) {
        updateProperty(userId, property -> property.setThreads(property.getThreads() + 1));
    }
    
    default void decrementThreads(Long userId) {
        updateProperty(userId, property -> {
            if (property.getThreads() > 0) {
                property.setThreads(property.getThreads() - 1);
            }
        });
    }
    
    default void incrementComments(Long userId) {
        updateProperty(userId, property -> property.setComments(property.getComments() + 1));
    }
    
    default void decrementComments(Long userId) {
        updateProperty(userId, property -> {
            if (property.getComments() > 0) {
                property.setComments(property.getComments() - 1);
            }
        });
    }
    
    default void incrementFans(Long userId) {
        updateProperty(userId, property -> property.setFans(property.getFans() + 1));
    }
    
    default void decrementFans(Long userId) {
        updateProperty(userId, property -> {
            if (property.getFans() > 0) {
                property.setFans(property.getFans() - 1);
            }
        });
    }
    
    default void addCredits(Long userId, int amount) {
        updateProperty(userId, property -> property.setCredits(property.getCredits() + amount));
    }
    
    default void addGolds(Long userId, int amount) {
        updateProperty(userId, property -> property.setGolds(property.getGolds() + amount));
    }
    
    default void updateProperty(Long userId, Consumer<ForumUserPropertyPO> updater) {
        ForumUserPropertyPO property = getByUserId(userId);
        if (property == null) {
            property = createUserProperty(userId);
        }
        
        // 更新属性
        updater.accept(property);
        BaseEntityUtils.setUpdateBaseEntity(property);
        
        // 保存更新
        updateById(property);
    }
} 