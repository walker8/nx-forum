package com.leyuz.uc.user;

import cn.hutool.extra.spring.SpringUtil;
import com.leyuz.module.cache.GenericCache;
import com.leyuz.uc.domain.auth.token.TokenGateway;
import com.leyuz.uc.domain.user.gateway.UserGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Token服务
 *
 * @author Walker
 */
@Service
@RequiredArgsConstructor
public class TokenApplication {
    private final TokenGateway tokenGateway;
    private final GenericCache<String, Long> userTokenCache;

    public Long getUserIdByToken(String token) {
        return userTokenCache.computeIfAbsent(token, t -> {
            Long userId = tokenGateway.findUserIdByToken(t);
            SpringUtil.getBean(UserGateway.class).updateLastActiveDate(userId);
            return userId;
        });
    }
}