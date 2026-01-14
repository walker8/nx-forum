import Http from '../utils/request'

// Types
export type DailyStatsVO = {
    statDate: string
    uniqueIpCount: number
    guestIpCount: number
    userIpCount: number
    threadCount: number
    commentCount: number
    terminalType: string
    platform: string
}

export type StatsOverviewVO = {
    totalThreads: number
    totalComments: number
    totalUsers: number
    todayUniqueIps: number
    todayGuestIps: number
    todayUserIps: number
    todayThreads: number
    todayComments: number
    todayNewUsers?: number
}

export type StatsTrendVO = {
    dates: string[]
    uniqueIps: number[]
    guestIps: number[]
    userIps: number[]
    threadCounts: number[]
    commentCounts: number[]
}

export type DailyStatsQuery = {
    startDate?: string
    endDate?: string
    terminalType?: string
    platform?: string
}

// API Methods
export const getDailyStats = (params: DailyStatsQuery) => {
    return Http.get('/v1/stats/daily', {params})
}

export const getStatsOverview = () => {
    return Http.get('/v1/stats/overview')
}

export const getStatsTrend = (params: {
    days?: number
    terminalType?: string
    platform?: string
}) => {
    return Http.get('/v1/stats/trend', params)
}

export const getStatsByTerminal = (params: {
    startDate: string
    endDate: string
}) => {
    return Http.get('/v1/stats/by-terminal', params)
}

export const getStatsByPlatform = (params: {
    startDate: string
    endDate: string
}) => {
    return Http.get('/v1/stats/by-platform', params)
}
