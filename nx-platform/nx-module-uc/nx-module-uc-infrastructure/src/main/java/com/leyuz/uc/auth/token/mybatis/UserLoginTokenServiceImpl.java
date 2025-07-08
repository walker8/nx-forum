package com.leyuz.uc.auth.token.mybatis;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyuz.uc.auth.token.UserLoginTokenPO;
import com.leyuz.uc.auth.token.mybatis.mapper.UserLoginTokenMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author walker
 * @since 2024-04-22
 */
@Service
public class UserLoginTokenServiceImpl extends ServiceImpl<UserLoginTokenMapper, UserLoginTokenPO> implements IUserLoginTokenService {

}
