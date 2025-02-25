package com.leyuz.uc.user.auth;

import com.leyuz.module.cache.GenericCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginFailureService {

    private final GenericCache<String, Integer> loginFailCountCache;
    private final GenericCache<String, Boolean> loginLockCache;

    public static final int MAX_FAILURE_COUNT = 5;

    public int recordLoginFailure(String username) {

        if (loginFailCountCache == null || loginLockCache == null) {
            throw new IllegalStateException("Cache not initialized");
        }

        // 获取当前失败次数
        Integer count = loginFailCountCache.get(username);
        count = (count == null ? 0 : count) + 1;

        // 更新失败次数
        loginFailCountCache.put(username, count);

        // 如果达到最大失败次数，则锁定账户
        if (count >= MAX_FAILURE_COUNT) {
            loginLockCache.put(username, true);
        }

        return count;
    }

    public void clearLoginFailure(String username) {
        if (loginLockCache != null) {
            loginLockCache.remove(username);
        }
        if (loginFailCountCache != null) {
            loginFailCountCache.remove(username);
        }
    }

    public boolean isUserLocked(String username) {
        Boolean locked = loginLockCache.get(username);
        return Boolean.TRUE.equals(locked);
    }
} 