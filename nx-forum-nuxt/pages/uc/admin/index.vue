<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  User,
  UserFilled,
  RefreshRight
} from '@element-plus/icons-vue'
import type { EChartsOption } from 'echarts'
import type {
  UserStatsOverviewVO,
  UserRegistrationTrendVO
} from '~/apis/uc/statistics'
import {
  getUserStatsOverview,
  getUserRegistrationTrend
} from '~/apis/uc/statistics'

definePageMeta({
  layout: 'uc'
})

useHead({
  title: '用户中心管理 - NX Forum'
})

// ============================================
// STATE
// ============================================
const loading = ref(true)
const refreshing = ref(false)

const overview = ref<UserStatsOverviewVO | null>(null)
const trendData = ref<UserRegistrationTrendVO | null>(null)

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
      textStyle: { color: '#606266', fontFamily: 'JetBrains Mono' },
      axisPointer: {
        type: 'cross',
        crossStyle: { color: '#409eff' }
      }
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
      axisLine: { lineStyle: { color: '#e4e7ed' } },
      axisLabel: { color: '#909399', fontFamily: 'JetBrains Mono', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#e4e7ed', type: 'dashed' } },
      axisLabel: { color: '#909399', fontFamily: 'JetBrains Mono', fontSize: 11 }
    },
    series: [
      {
        name: '新增用户',
        type: 'line',
        smooth: true,
        data: trendData.value.newUsersCounts,
        lineStyle: { color: '#409eff', width: 3 },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(64, 158, 255, 0.2)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0)' }
            ]
          }
        },
        itemStyle: { color: '#409eff' },
        emphasis: { focus: 'series' }
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

    const [overviewRes, trendRes] = await Promise.all([
      getUserStatsOverview(),
      getUserRegistrationTrend({ days: 30 })
    ])

    overview.value = overviewRes.data
    trendData.value = trendRes.data

  } catch (error) {
    console.error('Failed to fetch user statistics:', error)
    ElMessage.error('加载数据失败，请稍后重试')
  } finally {
    loading.value = false
    refreshing.value = false
  }
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
      <!-- Total Users -->
      <el-col :xs="24" :sm="12" :lg="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #ecf5ff;">
              <el-icon :size="32" color="#409eff">
                <User />
              </el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">总用户数</p>
              <p class="stat-value">{{ formatNumber(overview?.totalUsers || 0) }}</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Today's New Users -->
      <el-col :xs="24" :sm="12" :lg="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f0f9ff;">
              <el-icon :size="32" color="#67c23a">
                <UserFilled />
              </el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">今日新增用户</p>
              <p class="stat-value">{{ overview?.todayNewUsers || 0 }}</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Today's Active Users -->
      <el-col :xs="24" :sm="12" :lg="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fdf6ec;">
              <el-icon :size="32" color="#e6a23c">
                <UserFilled />
              </el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">今日活跃用户</p>
              <p class="stat-value">{{ overview?.todayActiveUsers || 0 }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Trend Chart Section -->
    <el-card shadow="never" style="margin-bottom: 20px;" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="card-title">用户注册趋势（最近30天）</span>
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
  </div>
</template>

<style scoped>
.stat-card {
  border-radius: 8px;
  background: #ffffff;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
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
</style>
