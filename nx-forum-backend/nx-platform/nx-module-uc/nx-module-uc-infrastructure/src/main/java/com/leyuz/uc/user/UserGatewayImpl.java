package com.leyuz.uc.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.leyuz.common.utils.BaseEntityUtils;
import com.leyuz.common.utils.HeaderUtils;
import com.leyuz.uc.user.dataobject.UserStatusV;
import com.leyuz.uc.user.gateway.UserGateway;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class UserGatewayImpl implements UserGateway {
    private final UserMapper userMapper;
    private final PasswordEncoder encoder;

    @Override
    public UserE save(UserE userE) {
        if (userE != null) {
            UserPO userPO = new UserPO();
            userPO.setUserName(userE.getUserName());
            userPO.setEmail(userE.getEmail());
            userPO.setPhone(userE.getPhone());
            if (StringUtils.isNotEmpty(userE.getPassword())) {
                userPO.setPassword(encoder.encode(userE.getPassword()));
            }
            userPO.setAvatar(userE.getAvatar());
            userPO.setLastActiveIp(userE.getLastActiveIp());
            userPO.setLastActiveDate(userE.getLastActiveDate());
            userPO.setAccountStatus((byte) userE.getStatus().getValue());
            BaseEntityUtils.setCreateBaseEntity(userPO);
            userMapper.insert(userPO);
            userE.setUserId(userPO.getUserId());
            return userE;
        }
        return null;
    }

    @Override
    public void update(UserE userE) {
        if (userE == null) {
            return;
        }
        UserPO userPO = new UserPO();
        if (StringUtils.isNotEmpty(userE.getUserName())) {
            userPO.setUserName(userE.getUserName());
        }
        if (StringUtils.isNotEmpty(userE.getEmail())) {
            userPO.setEmail(userE.getEmail());
        }
        if (StringUtils.isNotEmpty(userE.getPhone())) {
            userPO.setPhone(userE.getPhone());
        }
        if (StringUtils.isNotEmpty(userE.getPassword())) {
            userPO.setPassword(encoder.encode(userE.getPassword()));
        }
        if (StringUtils.isNotEmpty(userE.getAvatar())) {
            userPO.setAvatar(userE.getAvatar());
        }
        if (StringUtils.isNotEmpty(userE.getIntro())) {
            userPO.setIntro(userE.getIntro());
        }
        if (StringUtils.isNotEmpty(userE.getLastActiveIp())) {
            userPO.setLastActiveIp(userE.getLastActiveIp());
        }
        if (userE.getLastActiveDate() != null) {
            userPO.setLastActiveDate(userE.getLastActiveDate());
        }
        userPO.setUserId(userE.getUserId());
        BaseEntityUtils.setUpdateBaseEntity(userPO);
        userMapper.updateById(userPO);
    }

    @Override
    public UserE getById(long userId) {
        UserPO userPO = userMapper.selectById(userId);
        return convert(userPO);
    }

    @Override
    public UserE getByUserName(String userName) {
        if (StringUtils.isEmpty(userName)) {
            return null;
        }
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", userName);
        queryWrapper.eq("is_deleted", false);
        UserPO userPO = userMapper.selectOne(queryWrapper);
        return convert(userPO);
    }

    @Override
    public UserE getByPhone(String phone) {
        if (StringUtils.isEmpty(phone)) {
            return null;
        }
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        queryWrapper.eq("is_deleted", false);
        UserPO userPO = userMapper.selectOne(queryWrapper);
        return convert(userPO);
    }

    @Override
    public UserE getByEmail(String email) {
        if (StringUtils.isEmpty(email)) {
            return null;
        }
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        queryWrapper.eq("is_deleted", false);
        UserPO userPO = userMapper.selectOne(queryWrapper);
        return convert(userPO);
    }

    private UserE convert(UserPO userPO) {
        if (userPO != null) {
            return UserE.builder()
                    .userId(userPO.getUserId())
                    .userName(userPO.getUserName())
                    .phone(userPO.getPhone())
                    .email(userPO.getEmail())
                    .avatar(userPO.getAvatar())
                    .intro(userPO.getIntro())
                    .status(UserStatusV.of(userPO.getAccountStatus()))
                    .createTime(userPO.getCreateTime())
                    .lastActiveDate(userPO.getLastActiveDate())
                    // 密码校验时使用
                    .password(userPO.getPassword())
                    .build();
        }
        return null;
    }

    @Override
    public void deleteById(Long userId) {
        UserPO userPO = new UserPO();
        userPO.setUserId(userId);
        userPO.setIsDeleted(true);
        BaseEntityUtils.setUpdateBaseEntity(userPO);
        userMapper.updateById(userPO);
    }

    @Override
    public void updateLastActiveDate(Long userId) {
        if (userId != null && userId > 0) {
            UserPO userPO = new UserPO();
            userPO.setUserId(userId);
            userPO.setLastActiveIp(HeaderUtils.getIp());
            userPO.setLastActiveDate(LocalDateTime.now());
            userMapper.updateById(userPO);
        }
    }

    @Override
    public Long countTotalUsers() {
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false);
        return userMapper.selectCount(queryWrapper);
    }

    @Override
    public Long countUsersCreatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false);
        queryWrapper.ge("create_time", startDate);
        queryWrapper.le("create_time", endDate);
        return userMapper.selectCount(queryWrapper);
    }

    @Override
    public Long countUsersActiveBetween(LocalDateTime startDate, LocalDateTime endDate) {
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false);
        queryWrapper.ge("last_active_date", startDate);
        queryWrapper.le("last_active_date", endDate);
        return userMapper.selectCount(queryWrapper);
    }

    @Override
    public Map<LocalDate, Long> countUsersByDate(LocalDate startDate, LocalDate endDate) {
        // Convert LocalDate to LocalDateTime for query
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // Query users created between start and end date
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false);
        queryWrapper.ge("create_time", startDateTime);
        queryWrapper.le("create_time", endDateTime);
        queryWrapper.select("create_time");
        List<UserPO> users = userMapper.selectList(queryWrapper);

        // Group by date
        Map<LocalDate, Long> result = new java.util.TreeMap<>();
        for (UserPO user : users) {
            LocalDate date = user.getCreateTime().toLocalDate();
            result.put(date, result.getOrDefault(date, 0L) + 1);
        }

        // Fill in missing dates with 0
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            result.putIfAbsent(current, 0L);
            current = current.plusDays(1);
        }

        return result;
    }
}
