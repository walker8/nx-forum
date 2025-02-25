package com.leyuz.common.utils;

import com.leyuz.common.dto.BaseEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BaseEntityUtils {

    public static void setCreateBaseEntity(BaseEntity baseEntity) {
        if (baseEntity == null) {
            return;
        }
        baseEntity.setCreateTime(LocalDateTime.now());
        baseEntity.setCreateBy(HeaderUtils.getUserId());
        baseEntity.setUpdateTime(LocalDateTime.now());
        baseEntity.setUpdateBy(HeaderUtils.getUserId());
        baseEntity.setIsDeleted(false);
    }

    public static void setUpdateBaseEntity(BaseEntity baseEntity) {
        if (baseEntity == null) {
            return;
        }
        baseEntity.setCreateTime(null);
        baseEntity.setCreateBy(null);
        baseEntity.setUpdateTime(LocalDateTime.now());
        baseEntity.setUpdateBy(HeaderUtils.getUserId());
    }
}
