<script setup lang="ts">
import type {EChartsOption} from 'echarts'
import type {DailyStatsVO, StatsOverviewVO, StatsTrendVO} from '~/apis/statistics'
import {getStatsByPlatform, getStatsByTerminal, getStatsOverview, getStatsTrend} from '~/apis/statistics'
import {getUserStatsOverview} from '~/apis/uc/statistics'
import {computed, onMounted, onUnmounted, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {ChatDotRound, Connection, DataLine, RefreshRight, User} from '@element-plus/icons-vue'

definePageMeta({
  layout: 'admin'
})

useHead({
  title: '数据统计 - NX Forum'
})

// ============================================
// TYPES
// ============================================
type TimeRange = 'yesterday' | 'week' | 'month'

// ============================================
// STATE
// ============================================
const loading = ref(true)
const refreshing = ref(false)

const overview = ref<StatsOverviewVO | null>(null)
const trendData = ref<StatsTrendVO | null>(null)
const terminalBreakdown = ref<Record<string, DailyStatsVO>>({})
const osBreakdown = ref<Record<string, DailyStatsVO>>({})

// Time range selection for terminal and OS breakdown charts
const breakdownTimeRange = ref<TimeRange>('yesterday') // Default to yesterday

// Auto-refresh interval
let refreshInterval: NodeJS.Timeout | null = null

// ============================================
// COMPUTED
// ============================================
const trendChartOption = computed<EChartsOption>(() => {
  if (!trendData.value) return {}

  const dates = trendData.value.dates.map(d => {
    const date = new Date(d)
    return `${date.getMonth() + 1}/${date.getDate()}`
  })

  return {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e4e7ed',
      textStyle: {color: '#606266', fontFamily: 'JetBrains Mono'},
      axisPointer: {
        type: 'cross',
        crossStyle: {color: '#409eff'}
      }
    },
    legend: {
      data: ['用户访问量', '游客', '注册用户', '帖子数', '评论数'],
      textStyle: {color: '#606266', fontFamily: 'Inter'},
      top: 8,
      itemGap: 20
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLine: {lineStyle: {color: '#e4e7ed'}},
      axisLabel: {color: '#909399', fontFamily: 'JetBrains Mono', fontSize: 11}
    },
    yAxis: {
      type: 'value',
      axisLine: {show: false},
      axisTick: {show: false},
      splitLine: {lineStyle: {color: '#e4e7ed', type: 'dashed'}},
      axisLabel: {color: '#909399', fontFamily: 'JetBrains Mono', fontSize: 11}
    },
    series: [
      {
        name: '用户访问量',
        type: 'line',
        smooth: true,
        data: trendData.value.uniqueIps,
        lineStyle: {color: '#409eff', width: 3},
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              {offset: 0, color: 'rgba(64, 158, 255, 0.2)'},
              {offset: 1, color: 'rgba(64, 158, 255, 0)'}
            ]
          }
        },
        itemStyle: {color: '#409eff'},
        emphasis: {focus: 'series'}
      },
      {
        name: '游客',
        type: 'line',
        smooth: true,
        data: trendData.value.guestIps,
        lineStyle: {color: '#67c23a', width: 2},
        itemStyle: {color: '#67c23a'},
        emphasis: {focus: 'series'}
      },
      {
        name: '注册用户',
        type: 'line',
        smooth: true,
        data: trendData.value.userIps,
        lineStyle: {color: '#e6a23c', width: 2},
        itemStyle: {color: '#e6a23c'},
        emphasis: {focus: 'series'}
      },
      {
        name: '帖子数',
        type: 'line',
        smooth: true,
        data: trendData.value.threadCounts,
        lineStyle: {color: '#909399', width: 2, type: 'dashed'},
        itemStyle: {color: '#909399'},
        emphasis: {focus: 'series'}
      },
      {
        name: '评论数',
        type: 'line',
        smooth: true,
        data: trendData.value.commentCounts,
        lineStyle: {color: '#f56c6c', width: 2, type: 'dashed'},
        itemStyle: {color: '#f56c6c'},
        emphasis: {focus: 'series'}
      }
    ]
  }
})

const terminalChartOption = computed<EChartsOption>(() => {
  const data = Object.entries(terminalBreakdown.value).map(([key, value]) => ({
    name: key === 'PC' ? 'PC端' : key === 'MOBILE' ? '移动端' : key === 'APP' ? 'APP' : key,
    value: value.uniqueIpCount
  }))

  return {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e4e7ed',
      textStyle: {color: '#606266', fontFamily: 'JetBrains Mono'},
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: {color: '#606266', fontFamily: 'Inter'}
    },
    series: [
      {
        type: 'pie',
        radius: ['45%', '75%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {show: false},
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold',
            color: '#303133',
            fontFamily: 'Inter'
          }
        },
        labelLine: {show: false},
        data: data,
        color: ['#409eff', '#67c23a', '#e6a23c']
      }
    ]
  }
})

const osChartOption = computed<EChartsOption>(() => {
  const data = Object.entries(osBreakdown.value).map(([key, value]) => ({
    name: key,
    value: value.uniqueIpCount
  }))

  return {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e4e7ed',
      textStyle: {color: '#606266', fontFamily: 'JetBrains Mono'},
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: '5%',
      top: 'center',
      textStyle: {color: '#606266', fontFamily: 'Inter'}
    },
    series: [
      {
        type: 'pie',
        radius: ['45%', '75%'],
        center: ['65%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {show: false},
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold',
            color: '#303133',
            fontFamily: 'Inter'
          }
        },
        labelLine: {show: false},
        data: data,
        color: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#606266']
      }
    ]
  }
})

// ============================================
// METHODS
// ============================================
const fetchData = async (showLoading = true) => {
  try {
    if (showLoading) {
      loading.value = true
    } else {
      refreshing.value = true
    }

    // Fetch breakdown stats based on selected time range
    const range = getDateRange(breakdownTimeRange.value)

    // Fetch forum statistics and user statistics in parallel
    const [overviewRes, trendRes, terminalRes, osRes, userStatsRes] = await Promise.all([
      getStatsOverview(),
      getStatsTrend({days: 30, terminalType: 'ALL', platform: 'ALL'}),
      getStatsByTerminal({
        startDate: range.start,
        endDate: range.end
      }),
      getStatsByPlatform({
        startDate: range.start,
        endDate: range.end
      }),
      getUserStatsOverview() // Fetch user stats from UC module
    ])

    // Combine forum stats and user stats
    overview.value = {
      ...overviewRes.data,
      todayNewUsers: userStatsRes.data.todayNewUsers
    }
    trendData.value = trendRes.data
    terminalBreakdown.value = terminalRes.data
    osBreakdown.value = osRes.data

  } catch (error) {
    console.error('Failed to fetch statistics:', error)
    ElMessage.error('加载数据失败，请稍后重试')
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const formatDate = (date: Date): string => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// Get date range based on selection
const getDateRange = (range: TimeRange) => {
  const now = new Date()
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)

  if (range === 'yesterday') {
    return {
      start: formatDate(yesterday),
      end: formatDate(yesterday)
    }
  } else if (range === 'week') {
    const weekAgo = new Date(now)
    weekAgo.setDate(weekAgo.getDate() - 7)
    return {
      start: formatDate(weekAgo),
      end: formatDate(now)
    }
  } else { // month
    const monthAgo = new Date(now)
    monthAgo.setDate(monthAgo.getDate() - 30)
    return {
      start: formatDate(monthAgo),
      end: formatDate(now)
    }
  }
}

const handleTimeRangeChange = (range: TimeRange) => {
  breakdownTimeRange.value = range
  fetchData(false) // Refresh without full loading
}

const handleRefresh = () => {
  fetchData(false)
  ElMessage.success('数据已刷新')
}

// Format large numbers
const formatNumber = (num: number): string => {
  if (num >= 1000000) return `${(num / 1000000).toFixed(1)}M`
  if (num >= 1000) return `${(num / 1000).toFixed(1)}K`
  return String(num)
}

// ============================================
// LIFECYCLE
// ============================================
onMounted(() => {
  fetchData()

  // Auto-refresh every 5 minutes
  refreshInterval = setInterval(() => {
    fetchData(false)
  }, 5 * 60 * 1000)
})

onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
})
</script>

<template>
  <div class="nx-container" style="background: #f5f7fa; min-height: 100vh; padding: 20px;">
    <!-- Overview Cards Section -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <!-- Threads Card -->
      <el-col :xs="24" :sm="12" :xl="6">
        <el-card shadow="hover" class="stat-card enhanced-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #ecf5ff;">
              <el-icon :size="40" color="#409eff">
                <ChatDotRound/>
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-total">
                <p class="stat-value">{{ formatNumber(overview?.totalThreads || 0) }}</p>
                <p class="stat-label">总帖数</p>
              </div>
              <div class="stat-today">
                <p class="stat-today-value">{{ overview?.todayThreads || 0 }}</p>
                <p class="stat-today-label">今日</p>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Comments Card -->
      <el-col :xs="24" :sm="12" :xl="6">
        <el-card shadow="hover" class="stat-card enhanced-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f0f9ff;">
              <el-icon :size="40" color="#67c23a">
                <DataLine/>
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-total">
                <p class="stat-value">{{ formatNumber(overview?.totalComments || 0) }}</p>
                <p class="stat-label">总评论数</p>
              </div>
              <div class="stat-today">
                <p class="stat-today-value">{{ overview?.todayComments || 0 }}</p>
                <p class="stat-today-label">今日</p>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Users Card -->
      <el-col :xs="24" :sm="12" :xl="6">
        <el-card shadow="hover" class="stat-card enhanced-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fdf6ec;">
              <el-icon :size="40" color="#e6a23c">
                <User/>
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-total">
                <p class="stat-value">{{ formatNumber(overview?.totalUsers || 0) }}</p>
                <p class="stat-label">总用户数</p>
              </div>
              <div class="stat-today">
                <p class="stat-today-value">{{ overview?.todayNewUsers || 0 }}</p>
                <p class="stat-today-label">今日注册</p>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Today's IPs Card -->
      <el-col :xs="24" :sm="12" :xl="6">
        <el-card shadow="hover" class="stat-card enhanced-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fef0f0;">
              <el-icon :size="40" color="#f56c6c">
                <Connection/>
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-total">
                <p class="stat-value">{{ overview?.todayUniqueIps || 0 }}</p>
                <p class="stat-label">今日用户访问量</p>
              </div>
              <div class="stat-today">
                <p class="stat-today-value">{{ overview?.todayUserIps || 0 }}</p>
                <p class="stat-today-label">注册用户</p>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Trend Chart Section -->
    <el-card shadow="never" style="margin-bottom: 20px;" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="card-title">数据趋势（最近30天）</span>
          <el-button type="primary" :icon="RefreshRight" @click="handleRefresh" size="small" :loading="refreshing">
            刷新
          </el-button>
        </div>
      </template>
      <v-chart
          v-if="trendData && !loading"
          :option="trendChartOption"
          style="height: 400px;"
          :autoresize="true"
      />
      <div v-else-if="!loading" class="chart-empty">
        暂无趋势数据
      </div>
    </el-card>

    <!-- Breakdown Charts Section -->
    <el-row :gutter="20">
      <!-- Terminal Breakdown -->
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" v-loading="loading">
          <template #header>
            <div class="card-header-with-control">
              <span class="card-title">终端类型分布</span>
              <el-radio-group v-model="breakdownTimeRange" @change="handleTimeRangeChange" size="small">
                <el-radio-button value="yesterday">昨天</el-radio-button>
                <el-radio-button value="week">最近一周</el-radio-button>
                <el-radio-button value="month">最近一个月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <v-chart
              v-if="Object.keys(terminalBreakdown).length > 0 && !loading"
              :option="terminalChartOption"
              style="height: 350px;"
              :autoresize="true"
          />
          <div v-else-if="!loading" class="chart-empty">
            暂无终端数据
          </div>
        </el-card>
      </el-col>

      <!-- OS Breakdown -->
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" v-loading="loading">
          <template #header>
            <div class="card-header-with-control">
              <span class="card-title">设备分布</span>
              <span class="sync-hint">（与终端类型同步）</span>
            </div>
          </template>
          <v-chart
              v-if="Object.keys(osBreakdown).length > 0 && !loading"
              :option="osChartOption"
              style="height: 350px;"
              :autoresize="true"
          />
          <div v-else-if="!loading" class="chart-empty">
            暂无系统数据
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.stat-card {
  border-radius: 8px;
  background: #ffffff;
  transition: all 0.3s ease;
  height: 100%;
}

.stat-card :deep(.el-card__body) {
  padding-top: 0;
  padding-bottom: 0;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
  min-height: 100px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin: 0 0 8px 0;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  font-family: 'JetBrains Mono', monospace;
}

/* Enhanced card styles */
.enhanced-card .stat-content {
  gap: 20px;
}

.enhanced-card .stat-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.stat-total {
  text-align: left;
}

.stat-total .stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin: 0;
  line-height: 1.2;
}

.stat-total .stat-label {
  font-size: 13px;
  color: #909399;
  margin: 4px 0 0 0;
}

.stat-today {
  text-align: right;
  background: #f5f7fa;
  padding: 8px 16px;
  border-radius: 8px;
  min-width: 80px;
}

.stat-today-value {
  font-size: 20px;
  font-weight: 600;
  color: #409eff;
  margin: 0;
  line-height: 1.2;
}

.stat-today-label {
  font-size: 12px;
  color: #909399;
  margin: 2px 0 0 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header-with-control {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.sync-hint {
  font-size: 12px;
  color: #909399;
}

.chart-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 350px;
  font-size: 14px;
  color: #909399;
}

/* Responsive design */
@media (max-width: 768px) {
  .nx-container {
    padding: 10px !important;
  }

  .stat-value {
    font-size: 20px;
  }

  .stat-icon {
    width: 48px;
    height: 48px;
  }
}

/* Add spacing for 2x2 layout on medium screens */
@media (min-width: 769px) and (max-width: 1919px) {
  .el-col-sm-12:nth-child(n+3) {
    margin-top: 20px;
  }
}
</style>
