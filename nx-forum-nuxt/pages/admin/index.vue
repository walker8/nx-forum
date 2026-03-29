<script setup lang="ts">
import type {EChartsOption} from 'echarts'
import type {DashboardOverview} from '~/apis/admin/dashboard'
import type {Component} from 'vue'
import {getDashboardOverview} from '~/apis/admin/dashboard'
import {formatBytes, formatNumber} from '~/utils'
import {computed, onMounted, onUnmounted, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {
  ChatDotRound,
  Connection,
  DataLine,
  Monitor,
  RefreshRight,
  User
} from '@element-plus/icons-vue'

definePageMeta({
  layout: 'admin'
})

useHead({
  title: '管理后台 - NX Forum'
})

const loading = ref(true)
const refreshing = ref(false)
const overview = ref<DashboardOverview | null>(null)

let refreshInterval: NodeJS.Timeout | null = null

const CHART_BASE = {
  backgroundColor: 'transparent' as const,
  tooltip: {
    trigger: 'axis' as const,
    backgroundColor: 'rgba(255, 255, 255, 0.95)',
    borderColor: '#e4e7ed',
    textStyle: {color: '#606266', fontFamily: 'JetBrains Mono'},
    axisPointer: {
      type: 'cross' as const,
      crossStyle: {color: '#409eff'}
    }
  },
  legend: {
    data: ['主题数', '评论数', '新用户'],
    textStyle: {color: '#606266', fontFamily: 'Inter'},
    top: 8,
    itemGap: 20
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  }
}

const CHART_AXIS_STYLE = {
  xAxis: {
    type: 'category' as const,
    boundaryGap: false,
    axisLine: {lineStyle: {color: '#e4e7ed'}},
    axisLabel: {color: '#909399', fontFamily: 'JetBrains Mono', fontSize: 11}
  },
  yAxis: {
    type: 'value' as const,
    axisLine: {show: false},
    axisTick: {show: false},
    splitLine: {lineStyle: {color: '#e4e7ed', type: 'dashed'}},
    axisLabel: {color: '#909399', fontFamily: 'JetBrains Mono', fontSize: 11}
  }
}

const trendChartOption = computed<EChartsOption>(() => {
  if (!overview.value?.trendDates?.length) return {}

  return {
    ...CHART_BASE,
    xAxis: {...CHART_AXIS_STYLE.xAxis, data: overview.value.trendDates},
    yAxis: CHART_AXIS_STYLE.yAxis,
    series: [
      {
        name: '主题数',
        type: 'line',
        smooth: true,
        data: overview.value.trendThreads,
        lineStyle: {color: '#409eff', width: 3},
        areaStyle: {
          color: {
            type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
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
        name: '评论数',
        type: 'line',
        smooth: true,
        data: overview.value.trendComments,
        lineStyle: {color: '#67c23a', width: 2},
        itemStyle: {color: '#67c23a'},
        emphasis: {focus: 'series'}
      },
      {
        name: '新用户',
        type: 'line',
        smooth: true,
        data: overview.value.trendNewUsers,
        lineStyle: {color: '#e6a23c', width: 2},
        itemStyle: {color: '#e6a23c'},
        emphasis: {focus: 'series'}
      }
    ]
  }
})

interface StatCardConfig {
  icon: Component
  iconColor: string
  iconBg: string
  value: string
  label: string
  todayValue: string
  todayLabel: string
}

const statCards = computed<StatCardConfig[]>(() => {
  if (!overview.value) return []
  const o = overview.value
  return [
    {icon: User, iconColor: '#409eff', iconBg: '#ecf5ff', value: formatNumber(o.totalUsers), label: '总用户', todayValue: String(o.todayNewUsers), todayLabel: '今日注册'},
    {icon: ChatDotRound, iconColor: '#67c23a', iconBg: '#f0f9ff', value: formatNumber(o.totalThreads), label: '总主题', todayValue: String(o.todayThreads), todayLabel: '今日'},
    {icon: DataLine, iconColor: '#e6a23c', iconBg: '#fdf6ec', value: formatNumber(o.totalComments), label: '总评论', todayValue: String(o.todayComments), todayLabel: '今日'},
    {icon: Connection, iconColor: '#f56c6c', iconBg: '#fef0f0', value: String(o.todayUniqueIps), label: '今日访问量', todayValue: String(o.todayActiveUsers), todayLabel: '活跃用户'}
  ]
})

const fetchData = async (showLoading = true) => {
  try {
    if (showLoading) {
      loading.value = true
    } else {
      refreshing.value = true
    }

    const res = await getDashboardOverview()
    if (!overview.value || JSON.stringify(overview.value) !== JSON.stringify(res.data)) {
      overview.value = res.data
    }
  } catch (error) {
    console.error('Failed to fetch dashboard data:', error)
    ElMessage.error('加载数据失败，请稍后重试')
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const handleRefresh = async () => {
  await fetchData(false)
  ElMessage.success('数据已刷新')
}

onMounted(() => {
  fetchData()
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
  <div class="nx-container" style="background: #f5f7fa; min-height: 100vh; padding: 20px;" v-loading="loading">
    <template v-if="overview">
      <!-- Header -->
      <div class="dashboard-header">
        <div>
          <h2 class="dashboard-title">管理后台</h2>
          <p class="dashboard-subtitle">NX Forum {{ overview.appVersion }} | 系统运行中</p>
        </div>
        <el-button type="primary" :icon="RefreshRight" @click="handleRefresh" :loading="refreshing" size="small">
          刷新数据
        </el-button>
      </div>

      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col v-for="card in statCards" :key="card.label" :xs="24" :sm="12" :xl="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" :style="{background: card.iconBg}">
                <el-icon :size="40" :color="card.iconColor">
                  <component :is="card.icon"/>
                </el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-total">
                  <p class="stat-value">{{ card.value }}</p>
                  <p class="stat-label">{{ card.label }}</p>
                </div>
                <div class="stat-today">
                  <p class="stat-today-value">{{ card.todayValue }}</p>
                  <p class="stat-today-label">{{ card.todayLabel }}</p>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- Row 2: Trend Chart + Pending Items -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :xs="24" :lg="14">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span class="card-title">近 7 天趋势</span>
                <NuxtLink to="/admin/statistics">
                  <el-button type="primary" link size="small">查看详细统计</el-button>
                </NuxtLink>
              </div>
            </template>
            <v-chart
              v-if="overview.trendDates?.length"
              :option="trendChartOption"
              style="height: 350px;"
              :autoresize="true"
            />
            <div v-else class="chart-empty">
              暂无趋势数据
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :lg="10">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span class="card-title">待处理事项</span>
                <el-tag v-if="overview.totalAuditCount > 0" type="danger" size="small" round>
                  {{ overview.totalAuditCount }}
                </el-tag>
                <el-tag v-else type="success" size="small" round>已清空</el-tag>
              </div>
            </template>
            <div class="pending-list">
              <NuxtLink to="/admin/thread?status=auditing" class="pending-item">
                <span class="pending-label">待审核主题</span>
                <span class="pending-count" :class="{highlight: overview.threadAuditCount > 0}">
                  {{ overview.threadAuditCount }}
                </span>
              </NuxtLink>
              <NuxtLink to="/admin/comment?status=auditing" class="pending-item">
                <span class="pending-label">待审核评论</span>
                <span class="pending-count" :class="{highlight: overview.commentAuditCount > 0}">
                  {{ overview.commentAuditCount }}
                </span>
              </NuxtLink>
              <NuxtLink to="/admin/reply?status=auditing" class="pending-item">
                <span class="pending-label">待审核回复</span>
                <span class="pending-count" :class="{highlight: overview.replyAuditCount > 0}">
                  {{ overview.replyAuditCount }}
                </span>
              </NuxtLink>
              <NuxtLink to="/admin/report" class="pending-item">
                <span class="pending-label">待处理举报</span>
                <span class="pending-count" :class="{highlight: overview.pendingReportCount > 0}">
                  {{ overview.pendingReportCount }}
                </span>
              </NuxtLink>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- Row 3: System Info + Project Info -->
      <el-row :gutter="20">
        <el-col :xs="24" :lg="14">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span class="card-title">
                  <el-icon style="vertical-align: -2px;"><Monitor/></el-icon>
                  系统信息
                </span>
              </div>
            </template>
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="应用版本">
                <el-tag size="small">{{ overview.appVersion }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="运行时间">{{ overview.jvmUptime }}</el-descriptions-item>
              <el-descriptions-item label="Java 版本">{{ overview.javaVersion }}</el-descriptions-item>
              <el-descriptions-item label="Spring Boot">{{ overview.springBootVersion }}</el-descriptions-item>
              <el-descriptions-item label="操作系统">{{ overview.osName }} {{ overview.osVersion }}</el-descriptions-item>
              <el-descriptions-item label="系统架构">{{ overview.osArch }}</el-descriptions-item>
              <el-descriptions-item label="JVM 内存">
                {{ formatBytes(overview.jvmUsedMemory) }} / {{ formatBytes(overview.jvmMaxMemory) }}
              </el-descriptions-item>
              <el-descriptions-item label="内存使用率">
                <el-progress
                  :percentage="overview.jvmMaxMemory > 0 ? Number((overview.jvmUsedMemory / overview.jvmMaxMemory * 100).toFixed(1)) : 0"
                  :stroke-width="14"
                  :color="overview.jvmMaxMemory > 0 && overview.jvmUsedMemory / overview.jvmMaxMemory > 0.8 ? '#f56c6c' : '#409eff'"
                  style="width: 120px;"
                />
              </el-descriptions-item>
              <el-descriptions-item label="数据库">
                {{ overview.databaseType }} {{ overview.databaseVersion }}
              </el-descriptions-item>
              <el-descriptions-item label="缓存">
                <el-tag :type="overview.cacheEnabled ? 'success' : 'info'" size="small">
                  {{ overview.cacheEnabled ? 'Redis 已启用' : '本地缓存' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="磁盘空间">
                {{ formatBytes(overview.diskUsed) }} / {{ formatBytes(overview.diskTotal) }}
              </el-descriptions-item>
              <el-descriptions-item label="磁盘使用率">
                <el-progress
                  :percentage="overview.diskTotal > 0 ? Number((overview.diskUsed / overview.diskTotal * 100).toFixed(1)) : 0"
                  :stroke-width="14"
                  :color="overview.diskTotal > 0 && overview.diskUsed / overview.diskTotal > 0.9 ? '#f56c6c' : '#67c23a'"
                  style="width: 120px;"
                />
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>

        <el-col :xs="24" :lg="10">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span class="card-title">项目信息</span>
              </div>
            </template>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="项目名称">{{ overview.projectName }}</el-descriptions-item>
              <el-descriptions-item label="开源协议">
                <el-tag size="small" type="info">{{ overview.license }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="GitHub">
                <a :href="overview.githubUrl" target="_blank" class="github-link">
                  {{ overview.githubUrl }}
                  <el-icon style="vertical-align: -2px; margin-left: 4px;"><Connection/></el-icon>
                </a>
              </el-descriptions-item>
            </el-descriptions>

            <!-- Quick Links -->
            <div class="quick-links">
              <p class="quick-links-title">快捷操作</p>
              <div class="quick-links-grid">
                <NuxtLink to="/admin/base" class="quick-link-item">
                  <el-button size="small">站点设置</el-button>
                </NuxtLink>
                <NuxtLink to="/admin/thread" class="quick-link-item">
                  <el-button size="small">主题管理</el-button>
                </NuxtLink>
                <NuxtLink to="/admin/statistics" class="quick-link-item">
                  <el-button size="small">数据统计</el-button>
                </NuxtLink>
                <NuxtLink to="/admin/audit" class="quick-link-item">
                  <el-button size="small">审核设置</el-button>
                </NuxtLink>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<style scoped>
/* Header */
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.dashboard-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.dashboard-subtitle {
  font-size: 13px;
  color: #909399;
  margin: 4px 0 0 0;
}

/* Stat Cards - matching statistics.vue exactly */
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
  gap: 20px;
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.stat-total {
  text-align: left;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin: 0;
  line-height: 1.2;
  font-family: 'JetBrains Mono', monospace;
}

.stat-label {
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
  font-family: 'JetBrains Mono', monospace;
}

.stat-today-label {
  font-size: 12px;
  color: #909399;
  margin: 2px 0 0 0;
}

/* Card Headers */
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

/* Chart Empty */
.chart-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 350px;
  font-size: 14px;
  color: #909399;
}

/* Pending Items */
.pending-list {
  display: flex;
  flex-direction: column;
}

.pending-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
  text-decoration: none;
  color: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
}

.pending-item:last-child {
  border-bottom: none;
}

.pending-item:hover {
  background: #f5f7fa;
  padding-left: 12px;
  padding-right: 12px;
  margin: 0 -12px;
  border-radius: 6px;
}

.pending-label {
  font-size: 14px;
  color: #606266;
}

.pending-count {
  font-size: 20px;
  font-weight: 600;
  color: #c0c4cc;
  font-family: 'JetBrains Mono', monospace;
}

.pending-count.highlight {
  color: #f56c6c;
}

/* System & Project Info */
.github-link {
  color: #409eff;
  text-decoration: none;
  font-size: 13px;
  word-break: break-all;
}

.github-link:hover {
  text-decoration: underline;
}

/* Quick Links */
.quick-links {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.quick-links-title {
  font-size: 13px;
  color: #909399;
  margin: 0 0 12px 0;
}

.quick-links-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.quick-link-item {
  text-decoration: none;
}

/* Responsive - matching statistics.vue */
@media (max-width: 768px) {
  .nx-container {
    padding: 10px !important;
  }

  .dashboard-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .stat-value {
    font-size: 20px;
  }

  .stat-icon {
    width: 48px;
    height: 48px;
  }

  .stat-today {
    padding: 6px 12px;
    min-width: 64px;
  }

  .stat-today-value {
    font-size: 16px;
  }
}

@media (min-width: 769px) and (max-width: 1919px) {
  .el-col-sm-12:nth-child(n+3) {
    margin-top: 20px;
  }
}
</style>
