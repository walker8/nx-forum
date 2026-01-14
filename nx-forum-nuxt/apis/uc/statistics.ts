import Http from '../../utils/request'

// Types
export type UserStatsOverviewVO = {
    totalUsers: number
    todayNewUsers: number
    todayActiveUsers: number
}

export type UserRegistrationTrendVO = {
    dates: string[]
    newUsersCounts: number[]
}

// API Methods
/**
 * Get user statistics overview
 * @returns UserStatsOverviewVO containing total users, today's new users, and today's active users
 */
export const getUserStatsOverview = () => {
    return Http.get<UserStatsOverviewVO>('/v1/uc/admin/stats/overview')
}

/**
 * Get user registration trend
 * @param params - Query parameters
 * @param params.days - Number of days to look back (default: 30)
 * @returns UserRegistrationTrendVO containing dates and registration counts
 */
export const getUserRegistrationTrend = (params?: {
    days?: number
}) => {
    return Http.get<UserRegistrationTrendVO>('/v1/uc/admin/stats/registration-trend', params)
}
