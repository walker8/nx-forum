package com.leyuz.uc.user.gateway;

import com.leyuz.uc.user.UserE;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface UserGateway {
    UserE save(UserE userE);

    UserE getById(long userId);

    UserE getByUserName(String userName);

    UserE getByPhone(String phone);

    UserE getByEmail(String email);

    void update(UserE userE);

    void deleteById(Long userId);

    void updateLastActiveDate(Long userId);

    /**
     * Count total users
     */
    Long countTotalUsers();

    /**
     * Count users created between start and end date
     */
    Long countUsersCreatedBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count users by last active date between start and end date
     */
    Long countUsersActiveBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get user registration count grouped by date
     *
     * @param startDate start date (inclusive)
     * @param endDate   end date (inclusive)
     * @return Map of date to user count
     */
    Map<LocalDate, Long> countUsersByDate(LocalDate startDate, LocalDate endDate);
}
