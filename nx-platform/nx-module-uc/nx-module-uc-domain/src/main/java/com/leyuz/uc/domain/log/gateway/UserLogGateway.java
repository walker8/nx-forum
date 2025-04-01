package com.leyuz.uc.domain.log.gateway;

import com.leyuz.uc.domain.log.UserLogE;

import java.util.List;

/**
 * 用户日志网关接口
 */
public interface UserLogGateway {

    /**
     * 保存用户日志
     *
     * @param userLogE 用户日志实体
     */
    void save(UserLogE userLogE);

    /**
     * 根据ID获取用户日志
     *
     * @param logId 日志ID
     * @return 用户日志实体
     */
    UserLogE getById(Long logId);

    /**
     * 根据用户ID获取用户日志列表
     *
     * @param userId 用户ID
     * @return 用户日志实体列表
     */
    List<UserLogE> getByUserId(Long userId);
} 