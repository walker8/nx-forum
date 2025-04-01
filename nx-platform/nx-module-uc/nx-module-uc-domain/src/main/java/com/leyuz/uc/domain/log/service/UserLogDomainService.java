package com.leyuz.uc.domain.log.service;

import com.leyuz.uc.domain.log.UserLogE;
import com.leyuz.uc.domain.log.gateway.UserLogGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用户日志领域服务
 */
@Service
@RequiredArgsConstructor
public class UserLogDomainService {
    
    private final UserLogGateway userLogGateway;
    
    /**
     * 记录用户日志
     *
     * @param userLogE 用户日志实体
     */
    public void recordLog(UserLogE userLogE) {
        userLogE.record();
        userLogGateway.save(userLogE);
    }
} 