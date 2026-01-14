package com.leyuz.uc.user.dto;

import com.alibaba.cola.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * User Statistics Overview VO
 *
 * @author Walker
 * @since 2025-01-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsOverviewVO extends DTO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Total user count
     */
    private Long totalUsers;

    /**
     * Today's new user registrations
     */
    private Long todayNewUsers;

    /**
     * Today's active users (users who logged in today)
     */
    private Long todayActiveUsers;
}
