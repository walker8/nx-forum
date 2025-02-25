package com.leyuz.uc.auth.token;

public interface TokenGateway {
    void saveToken(Long userId, String token);

    Long findUserIdByToken(String token);

    boolean deleteByToken(String token);

    boolean deleteByDeviceId(String deviceId, Long userId);
}
