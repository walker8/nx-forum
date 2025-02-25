package com.leyuz.uc.user.gateway;

import com.leyuz.uc.user.UserE;

public interface UserGateway {
    UserE save(UserE userE);

    UserE getById(long userId);

    UserE getByUserName(String userName);

    UserE getByPhone(String phone);

    UserE getByEmail(String email);

    void update(UserE userE);

    void deleteById(Long userId);

    void updateLastActiveDate(Long userId);
}
