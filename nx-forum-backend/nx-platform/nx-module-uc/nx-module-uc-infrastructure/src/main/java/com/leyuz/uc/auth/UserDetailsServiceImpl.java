package com.leyuz.uc.auth;

import com.leyuz.common.exception.ValidationException;
import com.leyuz.uc.user.constant.RegexConstant;
import com.leyuz.uc.user.UserE;
import com.leyuz.uc.user.dataobject.UserStatusV;
import com.leyuz.uc.user.gateway.UserGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserGateway userGateway;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserE userE;
        if (Pattern.matches(RegexConstant.EMAIL_REGEX, username)) {
            userE = userGateway.getByEmail(username);
        } else if (Pattern.matches(RegexConstant.PHONE_REGEX, username)) {
            userE = userGateway.getByPhone(username);
        } else {
            userE = userGateway.getByUserName(username);
        }
        if (userE == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        if (UserStatusV.CANCELLED.equals(userE.getStatus())) {
            throw new ValidationException("用户已注销");
        }
        if (UserStatusV.DISABLED.equals(userE.getStatus())) {
            throw new ValidationException("用户已禁用");
        }
        AuthUserDetails authUserDetails = new AuthUserDetails();
        authUserDetails.setUserId(userE.getUserId());
        authUserDetails.setUsername(userE.getUserName());
        authUserDetails.setPassword(userE.getPassword());
        authUserDetails.setEnabled(true);
        return authUserDetails;
    }
}
