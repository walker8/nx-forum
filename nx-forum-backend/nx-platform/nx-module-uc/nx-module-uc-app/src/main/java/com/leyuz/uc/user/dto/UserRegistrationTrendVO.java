package com.leyuz.uc.user.dto;

import com.alibaba.cola.dto.DTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.time.LocalDate;
import java.util.List;

/**
 * User Registration Trend VO
 *
 * @author Walker
 * @since 2025-01-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationTrendVO extends DTO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * List of dates in the trend period
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private List<LocalDate> dates;

    /**
     * Number of new user registrations for each date
     */
    private List<Integer> newUsersCounts;
}
