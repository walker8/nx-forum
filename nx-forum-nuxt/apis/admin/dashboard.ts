import Http from '../../utils/request'

export interface DashboardOverview {
  // 核心统计
  totalUsers: number
  todayNewUsers: number
  todayActiveUsers: number
  totalThreads: number
  todayThreads: number
  totalComments: number
  todayComments: number
  todayUniqueIps: number
  // 待处理事项
  threadAuditCount: number
  commentAuditCount: number
  replyAuditCount: number
  pendingReportCount: number
  totalAuditCount: number
  // 趋势数据
  trendDates: string[]
  trendThreads: number[]
  trendComments: number[]
  trendNewUsers: number[]
  // 系统信息
  appVersion: string
  springBootVersion: string
  javaVersion: string
  osName: string
  osVersion: string
  osArch: string
  jvmTotalMemory: number
  jvmUsedMemory: number
  jvmMaxMemory: number
  jvmUptime: string
  diskTotal: number
  diskUsed: number
  diskFree: number
  databaseType: string
  databaseVersion: string
  cacheEnabled: boolean
  // 项目信息
  githubUrl: string
  license: string
  projectName: string
}

export const getDashboardOverview = () => {
  return Http.get('/v1/admin/dashboard/overview')
}
