package com.leyuz.uc.domain.user.gateway;

import com.leyuz.uc.domain.user.UserE;

public interface UserGateway {
    UserE save(UserE userE);

    UserE getById(long userId);

    UserE getByUserName(String userName);

    UserE getByPhone(String phone);

    UserE getByEmail(String email);

    void update(UserE userE);

    void deleteById(Long userId);
}
