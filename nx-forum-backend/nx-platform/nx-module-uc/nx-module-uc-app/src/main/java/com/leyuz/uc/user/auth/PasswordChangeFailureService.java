package com.leyuz.uc.user.auth;

import com.leyuz.module.cache.GenericCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordChangeFailureService {

    private final GenericCache<String, Integer> passwordChangeFailCountCache;

    public static final int MAX_FAILURE_COUNT = 5;
    public static final String LOCK_MESSAGE = "密码修改失败次数过多，请30分钟后再试";

    public int recordFailure(Long userId) {
        String key = String.valueOf(userId);
        Integer count = passwordChangeFailCountCache.get(key);
        count = (count == null ? 0 : count) + 1;
        passwordChangeFailCountCache.put(key, count);
        return count;
    }

    public void clearFailure(Long userId) {
        passwordChangeFailCountCache.remove(String.valueOf(userId));
    }

    public boolean isLocked(Long userId) {
        Integer count = passwordChangeFailCountCache.get(String.valueOf(userId));
        return count != null && count >= MAX_FAILURE_COUNT;
    }
}
