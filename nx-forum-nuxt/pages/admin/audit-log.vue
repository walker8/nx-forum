<template>
  <div class="nx-container">
    <el-card shadow="never">
      <div class="search-wrapper">
        <el-form ref="searchFormRef" :inline="true" :model="searchData" label-width="auto">
          <el-form-item prop="contentType" label="内容类型">
            <el-select
              v-model="searchData.contentType"
              placeholder="全部"
              clearable
              style="width: 140px"
            >
              <el-option
                v-for="(item, key) in CONTENT_TYPE_MAP"
                :key="key"
                :label="item.label"
                :value="Number(key)"
              />
            </el-select>
          </el-form-item>
          <el-form-item prop="userName" label="作者">
            <el-autocomplete
              v-model="searchData.userName"
              :fetch-suggestions="querySearchUser"
              placeholder="请输入用户名"
              value-key="userName"
              clearable
              style="width: 200px"
              @select="(item) => handleSearchUserSelect(item as UserVO)"
            />
          </el-form-item>
          <el-form-item prop="forumId" label="版块">
            <el-select
              v-model="searchData.forumId"
              placeholder="全部"
              clearable
              style="width: 160px"
            >
              <el-option
                v-for="item in forumMenus"
                :key="item.forumId"
                :label="item.nickName"
                :value="item.forumId"
              />
            </el-select>
          </el-form-item>
          <el-form-item prop="auditStage" label="审核阶段">
            <el-select
              v-model="searchData.auditStage"
              placeholder="全部"
              clearable
              style="width: 140px"
            >
              <el-option :value="1" label="黑白名单" />
              <el-option :value="2" label="敏感词" />
              <el-option :value="3" label="规则引擎" />
              <el-option :value="4" label="AI 审核" />
              <el-option :value="5" label="人工复审" />
            </el-select>
          </el-form-item>
          <el-form-item prop="auditStatusAfter" label="审核结果">
            <el-select
              v-model="searchData.auditStatusAfter"
              placeholder="全部"
              clearable
              style="width: 140px"
            >
              <el-option
                v-for="(item, key) in AUDIT_STATUS_MAP"
                :key="key"
                :label="item.label"
                :value="Number(key)"
              />
            </el-select>
          </el-form-item>
          <el-form-item prop="triggerSource" label="触发来源">
            <el-select
              v-model="searchData.triggerSource"
              placeholder="全部"
              clearable
              style="width: 140px"
            >
              <el-option :value="1" label="主动发布" />
              <el-option :value="2" label="编辑触发" />
              <el-option :value="3" label="举报触发" />
              <el-option :value="4" label="管理员后台" />
            </el-select>
          </el-form-item>
          <el-form-item prop="operatorType" label="操作人">
            <el-select
              v-model="searchData.operatorType"
              placeholder="全部"
              clearable
              style="width: 120px"
            >
              <el-option :value="1" label="系统" />
              <el-option :value="2" label="管理员" />
            </el-select>
          </el-form-item>
          <div class="el-form-item">
            <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
          </div>
        </el-form>
      </div>
      <div class="table-wrapper">
        <el-table :data="tableData" v-loading="loading" border>
          <el-table-column label="内容" width="150">
            <template #default="scope">
              <div class="content-cell">
                <el-tag size="small" :type="contentTypeTagType(scope.row.contentType)">
                  {{ CONTENT_TYPE_MAP[scope.row.contentType]?.label || scope.row.contentType }}
                </el-tag>
                <NuxtLink
                  v-if="canLink(scope.row)"
                  :to="buildContentLink(scope.row.contentType, scope.row.contentId)"
                  class="content-link"
                  target="_blank"
                >
                  #{{ scope.row.contentId }}
                  <el-icon style="margin-left: 2px; vertical-align: -1px"><Link /></el-icon>
                </NuxtLink>
                <span v-else-if="scope.row.contentId" class="content-id"
                  >#{{ scope.row.contentId }}</span
                >
                <span v-else class="content-id-muted">未保存</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="forumName" label="版块" align="center" width="120" />
          <el-table-column label="作者" align="center" width="130" show-overflow-tooltip>
            <template #default="scope">
              <NuxtLink
                v-if="scope.row.userId"
                :to="`/user/${scope.row.userId}`"
                target="_blank"
                class="user-link"
              >
                {{ scope.row.authorName || scope.row.userId }}
              </NuxtLink>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="triggerSourceName" label="触发来源" align="center" width="110" />
          <el-table-column label="审核阶段" align="center" width="110">
            <template #default="scope">
              <el-tag
                :type="AUDIT_STAGE_MAP[String(scope.row.auditStage)]?.type as any"
                size="small"
              >
                {{
                  AUDIT_STAGE_MAP[String(scope.row.auditStage)]?.label || scope.row.auditStageName
                }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="审核结果" align="center" width="100">
            <template #default="scope">
              <el-tag
                :type="AUDIT_STATUS_MAP[String(scope.row.auditStatusAfter)]?.type as any"
                size="small"
              >
                {{
                  scope.row.auditStatusAfterName ||
                  AUDIT_STATUS_MAP[String(scope.row.auditStatusAfter)]?.label
                }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="原因摘要" min-width="200" show-overflow-tooltip />
          <el-table-column label="IP 归属地" align="center" width="130" show-overflow-tooltip>
            <template #default="scope">
              <span v-if="scope.row.ipLocation">{{ scope.row.ipLocation }}</span>
              <span v-else-if="scope.row.userIp" class="ip-muted">{{ scope.row.userIp }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作人" align="center" width="130" show-overflow-tooltip>
            <template #default="scope">
              <el-tag v-if="scope.row.operatorType === 1" size="small" type="info">系统</el-tag>
              <span v-else-if="scope.row.operatorId">
                {{ scope.row.operatorName || scope.row.operatorId }}
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="costMs" label="耗时(ms)" align="center" width="90">
            <template #default="scope">
              <span v-if="scope.row.costMs">{{ scope.row.costMs }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="时间" align="center" width="180" />
          <el-table-column fixed="right" label="操作" width="100" align="center">
            <template #default="scope">
              <el-button type="primary" text bg size="small" @click="openDetail(scope.row)">
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pager-wrapper mt-4">
        <el-pagination
          background
          :layout="paginationData.layout"
          :page-sizes="paginationData.pageSizes"
          :total="paginationData.total"
          :page-size="paginationData.pageSize"
          :currentPage="paginationData.currentPage"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-drawer v-model="detailDrawerVisible" title="审核记录详情" size="700px" direction="rtl">
      <div v-if="currentLog" class="detail-container">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="日志ID">{{ currentLog.logId }}</el-descriptions-item>
          <el-descriptions-item label="会话ID">
            {{ currentLog.auditSessionId || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="内容">
            <el-tag size="small" :type="contentTypeTagType(currentLog.contentType)">
              {{ CONTENT_TYPE_MAP[currentLog.contentType]?.label || currentLog.contentType }}
            </el-tag>
            <span v-if="currentLog.contentId">
              <NuxtLink
                v-if="canLink(currentLog)"
                :to="buildContentLink(currentLog.contentType, currentLog.contentId)"
                target="_blank"
                class="content-link"
              >
                #{{ currentLog.contentId }}
              </NuxtLink>
              <span v-else>#{{ currentLog.contentId }}</span>
            </span>
            <span v-else style="color: #c0c4cc">未保存</span>
          </el-descriptions-item>
          <el-descriptions-item label="版块">
            {{ currentLog.forumName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="作者">
            <NuxtLink
              v-if="currentLog.userId"
              :to="`/users/${currentLog.userId}`"
              target="_blank"
              class="user-link"
            >
              {{ currentLog.authorName || currentLog.userId }}
            </NuxtLink>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="IP 归属地">
            <span v-if="currentLog.ipLocation">
              {{ currentLog.ipLocation }}
              <span class="ip-muted">({{ currentLog.userIp }})</span>
            </span>
            <span v-else-if="currentLog.userIp">{{ currentLog.userIp }}</span>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="触发来源">
            {{ currentLog.triggerSourceName }}
          </el-descriptions-item>
          <el-descriptions-item label="审核阶段">
            <el-tag
              :type="AUDIT_STAGE_MAP[String(currentLog.auditStage)]?.type as any"
              size="small"
            >
              {{ currentLog.auditStageName }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentLog.auditStatusBefore != null" label="审核前">
            {{ AUDIT_STATUS_MAP[String(currentLog.auditStatusBefore)]?.label }}
          </el-descriptions-item>
          <el-descriptions-item v-else label="审核前">
            <span style="color: #c0c4cc">新建</span>
          </el-descriptions-item>
          <el-descriptions-item label="审核后">
            <el-tag
              :type="AUDIT_STATUS_MAP[String(currentLog.auditStatusAfter)]?.type as any"
              size="small"
            >
              {{ currentLog.auditStatusAfterName }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作人">
            <el-tag v-if="currentLog.operatorType === 1" size="small" type="info">系统</el-tag>
            <NuxtLink
              v-else-if="currentLog.operatorId"
              :to="`/users/${currentLog.operatorId}`"
              target="_blank"
              class="user-link"
            >
              {{ currentLog.operatorName || currentLog.operatorId }}
            </NuxtLink>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="耗时"
            >{{ currentLog.costMs ?? '-' }} ms</el-descriptions-item
          >
          <el-descriptions-item label="创建时间" :span="2">{{
            currentLog.createTime
          }}</el-descriptions-item>
          <el-descriptions-item label="原因" :span="2">{{
            currentLog.reason || '-'
          }}</el-descriptions-item>
          <el-descriptions-item label="内容快照" :span="2">
            <div v-if="currentLog.contentSnapshot" class="snapshot">
              {{ currentLog.contentSnapshot }}
            </div>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="命中详情" :span="2">
            <pre v-if="hitDetailText" class="hit-detail">{{ hitDetailText }}</pre>
            <span v-else>-</span>
          </el-descriptions-item>
        </el-descriptions>
        <div class="related-actions">
          <el-button type="primary" @click="viewAllByContent" :disabled="!currentLog.contentId">
            查看此内容的所有审核记录
          </el-button>
        </div>
      </div>
    </el-drawer>

    <el-dialog v-model="relatedDialogVisible" title="该内容的所有审核记录" width="900px">
      <el-table :data="relatedLogs" border size="small">
        <el-table-column label="审核阶段" align="center" width="110">
          <template #default="scope">
            <el-tag :type="AUDIT_STAGE_MAP[String(scope.row.auditStage)]?.type as any" size="small">
              {{ scope.row.auditStageName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核结果" align="center" width="100">
          <template #default="scope">
            <el-tag
              :type="AUDIT_STATUS_MAP[String(scope.row.auditStatusAfter)]?.type as any"
              size="small"
            >
              {{ scope.row.auditStatusAfterName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="时间" align="center" width="160" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { Link, Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  AUDIT_STAGE_MAP,
  AUDIT_STATUS_MAP,
  CONTENT_TYPE_MAP,
  buildContentLink,
  type AuditLogVO,
  listAuditLogsByContent,
  pageAuditLogs
} from '~/apis/auditLog'
import { getAdminForumMenu } from '~/apis/forum'
import { queryUsername } from '~/apis/uc/user'
import type { UserVO } from '~/types/global'

definePageMeta({ layout: 'admin' })

const { paginationData, handleCurrentChange, handleSizeChange } = usePagination()

const searchData = reactive({
  contentType: undefined as number | undefined,
  userId: undefined as number | undefined,
  userName: '',
  forumId: undefined as number | undefined,
  auditStage: undefined as number | undefined,
  auditStatusAfter: undefined as number | undefined,
  triggerSource: undefined as number | undefined,
  operatorType: undefined as number | undefined
})

const tableData = ref<AuditLogVO[]>([])
const loading = ref(false)
const forumMenus = ref<Array<{ forumId: number; nickName: string }>>([])

const detailDrawerVisible = ref(false)
const currentLog = ref<AuditLogVO | null>(null)

const relatedDialogVisible = ref(false)
const relatedLogs = ref<AuditLogVO[]>([])

const contentTypeTagType = (t: number) => {
  if (t === 1) return 'primary'
  if (t === 2) return 'success'
  if (t === 3) return 'warning'
  return 'info'
}

// 主贴可以直接跳转；评论/楼中楼需要通过 threadId 中转，列表不展示
const canLink = (row: AuditLogVO) => {
  return !!(row.contentType === 1 && row.contentId)
}

// 命中详情 JSON 字符串（用于详情抽屉中的 <pre> 展示，保证缩进不被模板吞掉）
const hitDetailText = computed(() => {
  const obj = currentLog.value?.hitDetailObj
  if (!obj) return ''
  return JSON.stringify(obj, null, 2)
})

const enrich = (raw: any): AuditLogVO => {
  let hitDetailObj: Record<string, any> | undefined
  if (raw.hitDetail) {
    try {
      hitDetailObj = JSON.parse(raw.hitDetail)
    } catch {
      hitDetailObj = undefined
    }
  }
  return { ...raw, hitDetailObj }
}

const getTableData = () => {
  loading.value = true
  pageAuditLogs({
    pageNo: paginationData.currentPage,
    pageSize: paginationData.pageSize,
    contentType: searchData.contentType,
    userId: searchData.userId,
    forumId: searchData.forumId,
    auditStage: searchData.auditStage,
    auditStatusAfter: searchData.auditStatusAfter,
    triggerSource: searchData.triggerSource,
    operatorType: searchData.operatorType
  })
    .then((res: any) => {
      const records = (res.data?.records || []) as any[]
      tableData.value = records.map(enrich)
      paginationData.total = res.data?.total || 0
    })
    .catch((err: any) => {
      ElMessage.error(typeof err === 'string' ? err : '查询失败')
      tableData.value = []
    })
    .finally(() => {
      loading.value = false
    })
}

const handleSearch = () => {
  // 与 comment.vue 一致：避免与分页 watcher 同时触发导致重复请求
  if (paginationData.currentPage === 1) {
    getTableData()
  } else {
    paginationData.currentPage = 1
  }
}

const resetSearch = () => {
  Object.assign(searchData, {
    contentType: undefined,
    userId: undefined,
    userName: '',
    forumId: undefined,
    auditStage: undefined,
    auditStatusAfter: undefined,
    triggerSource: undefined,
    operatorType: undefined
  })
  lastSelectedUserName.value = ''
  // 与 comment.vue 一致：避免与分页 watcher 同时触发导致重复请求
  if (paginationData.currentPage === 1) {
    getTableData()
  } else {
    paginationData.currentPage = 1
  }
}

const querySearchUser = async (query: string) => {
  if (!query) return []
  try {
    const res = await queryUsername(query)
    return (res.data || []).map((item) => ({
      value: item.userName,
      ...item
    }))
  } catch (error) {
    return []
  }
}

const handleSearchUserSelect = (item: UserVO) => {
  searchData.userId = item.userId
  searchData.userName = item.userName
  lastSelectedUserName.value = item.userName
}

// 跟踪最近一次选中的用户名;输入框值不再等于它时,视为未选中,清空 userId
const lastSelectedUserName = ref('')
watch(
  () => searchData.userName,
  (newVal) => {
    if (newVal !== lastSelectedUserName.value) {
      searchData.userId = undefined
    }
  }
)

const openDetail = (row: AuditLogVO) => {
  currentLog.value = row
  detailDrawerVisible.value = true
}

const viewAllByContent = () => {
  if (!currentLog.value || !currentLog.value.contentId) {
    return
  }
  detailDrawerVisible.value = false
  listAuditLogsByContent(currentLog.value.contentType, currentLog.value.contentId)
    .then((res: any) => {
      relatedLogs.value = ((res.data || []) as any[]).map(enrich)
      relatedDialogVisible.value = true
    })
    .catch((err: any) => {
      ElMessage.error(typeof err === 'string' ? err : '查询失败')
    })
}

watch([() => paginationData.currentPage, () => paginationData.pageSize], getTableData, {
  immediate: true
})

onMounted(() => {
  getAdminForumMenu()
    .then((res: any) => {
      forumMenus.value = res.data || []
    })
    .catch(() => {
      forumMenus.value = []
    })
})
</script>

<style scoped>
.content-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.content-link {
  color: var(--el-color-primary);
  text-decoration: none;
}
.content-link:hover {
  text-decoration: underline;
}
.content-id {
  color: #606266;
}
.content-id-muted {
  color: #c0c4cc;
}
.user-link {
  color: var(--el-color-primary);
  text-decoration: none;
}
.user-link:hover {
  text-decoration: underline;
}
.ip-muted {
  color: #909399;
  font-size: 12px;
  margin-left: 4px;
}
.snapshot {
  background: #f5f7fa;
  padding: 8px;
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 200px;
  overflow-y: auto;
}
.hit-detail {
  background: #f5f7fa;
  padding: 8px;
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 300px;
  overflow-y: auto;
  font-size: 12px;
  margin: 0;
}
.related-actions {
  margin-top: 16px;
  text-align: center;
}
:deep(.el-drawer__header) {
  margin-bottom: 12px;
  padding-bottom: 12px;
}
:deep(.el-descriptions__label) {
  white-space: nowrap;
  width: 90px;
  min-width: 90px;
}
:deep(.el-descriptions__content) {
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
