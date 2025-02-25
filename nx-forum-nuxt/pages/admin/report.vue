<template>
  <div>
    <el-card>
      <template #header>
        <div>举报管理</div>
      </template>
      <el-form :model="query" inline>
        <el-form-item label="举报类型">
          <el-select v-model="query.targetType" clearable style="width: 120px">
            <el-option label="主题" value="THREAD" />
            <el-option label="评论" value="COMMENT" />
            <el-option label="回复" value="REPLY" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="query.handleStatus" clearable style="width: 180px">
            <el-option label="待处理" value="PENDING" />
            <el-option label="已处理-违规" value="APPROVED" />
            <el-option label="已处理-驳回" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="版块">
          <el-select v-model="query.forumId" placeholder="请选择版块" style="width: 180px" filterable clearable
            :disabled="forumId !== undefined && forumId > 0">
            <el-option v-for="item in forumMenus" :key="item.forumId" :label="item.nickName" :value="item.forumId">
              <span style="float: left">{{ item.nickName }}</span>
              <span style="float: right; color: var(--el-text-color-secondary); font-size: 13px">
                {{ item.name }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="举报人">
          <el-autocomplete v-model="reporterName" :fetch-suggestions="querySearchUser" placeholder="请输入举报人"
            @select="(item) => handleReporterSelect(item as UserVO)" value-key="userName" style="width: 180px" />
        </el-form-item>
        <el-form-item label="处理人">
          <el-autocomplete v-model="handlerName" :fetch-suggestions="querySearchUser" placeholder="请输入处理人"
            @select="(item) => handleHandlerSelect(item as UserVO)" value-key="userName" style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">搜索</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="reports" v-loading="loading">
        <el-table-column label="举报内容" width="450">
          <template #default="{ row }">
            <el-link :href="row.redirectUrl" target="_blank" type="primary" underline="never" v-if="row.redirectUrl">
              {{ row.reportedContent }}
            </el-link>
            <div v-else>
              {{ row.reportedContent }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="targetType" label="举报类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTargetTypeTag(row.targetType)">{{ getTargetType(row.targetType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reporterName" label="举报人" width="120" />
        <el-table-column prop="reportType" label="举报原因" width="120">
          <template #default="{ row }">
            {{ getReportType(row.reportType) }}
          </template>
        </el-table-column>
        <el-table-column prop="reportReason" label="补充说明" width="200" />
        <el-table-column prop="createTime" label="举报时间" width="180" />
        <el-table-column prop="handleStatus" label="处理状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getHandleStatusType(row.handleStatus)">
              {{ getHandleStatus(row.handleStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handlerName" label="处理人" width="120" />
        <el-table-column prop="handleReason" label="处理说明" />
        <el-table-column prop="updateTime" label="处理时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.handleStatus === 'PENDING'" type="primary" link @click="openHandleDialog(row)">
              处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager-wrapper">
        <el-pagination :current-page="pagination.currentPage" :page-size="pagination.pageSize" :total="pagination.total"
          @current-change="handleCurrentChange" @size-change="handleSizeChange"
          layout="total, sizes, prev, pager, next, jumper" />
      </div>
    </el-card>

    <el-dialog v-model="handleDialogVisible" title="处理举报" width="500px">
      <el-form :model="handleForm" label-width="80px">
        <el-form-item label="处理结果">
          <el-radio-group v-model="handleForm.handleStatus">
            <el-radio label="APPROVED" value="APPROVED">违规</el-radio>
            <el-radio label="REJECTED" value="REJECTED">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理说明">
          <el-input v-model="handleForm.handleReason" type="textarea" :rows="3" placeholder="请输入处理说明" />
        </el-form-item>
        <el-form-item label="通知用户">
          <el-switch v-model="handleForm.notifyUser" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="handleLoading" @click="submitHandle">
            提交
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { queryReports, handleReport } from '~/apis/report'
import { getAdminForumMenu } from '~/apis/forum'
import { queryUsername } from '~/apis/uc/user'
import type { ReportQuery, ReportInfo, HandleReportCommand, UserVO } from '~/types/global'

definePageMeta({
  layout: 'admin'
})

const route = useRoute()
const forumId = computed<number | undefined>(() => {
  const forumId = route.query.forumId
  return forumId ? Number(forumId) : undefined
})

const status = computed<string | undefined>(() => {
  return route.query.status as string | undefined
})

const reports = ref<ReportInfo[]>([])
const loading = ref(false)
const query = reactive<ReportQuery>({
  pageNo: 1,
  pageSize: 10,
  forumId: forumId.value,
})
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const reporterName = ref('')
const handlerName = ref('')

const forumMenus = useUserMenus()
if (forumMenus.value.length === 0) {
  getAdminForumMenu().then((res) => {
    forumMenus.value = res.data
  })
}

const handleDialogVisible = ref(false)
const handleLoading = ref(false)
const handleForm = reactive<HandleReportCommand>({
  reportId: 0,
  handleStatus: 'APPROVED',
  handleReason: '',
  notifyUser: true
})

// 如果路由参数中有status=pending，自动选择待处理状态
if (status.value === 'pending') {
  query.handleStatus = 'PENDING'
}

async function loadReports() {
  loading.value = true
  try {
    const res = await queryReports(query)
    reports.value = res.data.records
    pagination.total = res.data.total
  } finally {
    loading.value = false
  }
}

const querySearchUser = async (queryString: string) => {
  if (!queryString) return []
  try {
    const res = await queryUsername(queryString)
    return (res.data || []).map((item: UserVO) => ({
      value: item.userName,
      ...item
    }))
  } catch (error) {
    return []
  }
}

const handleReporterSelect = (item: UserVO) => {
  query.reporterId = item.userId
  reporterName.value = item.userName
}

const handleHandlerSelect = (item: UserVO) => {
  query.handlerId = item.userId
  handlerName.value = item.userName
}

function onSearch() {
  query.pageNo = 1
  loadReports()
}

function onReset() {
  query.targetType = undefined
  query.handleStatus = undefined
  query.forumId = forumId.value
  query.reporterId = undefined
  query.handlerId = undefined
  reporterName.value = ''
  handlerName.value = ''
  query.pageNo = 1
  loadReports()
}

function handleCurrentChange(page: number) {
  query.pageNo = page
  loadReports()
}

function handleSizeChange(size: number) {
  query.pageSize = size
  loadReports()
}

function getTargetType(type: string) {
  const types = {
    THREAD: '主题',
    COMMENT: '评论',
    REPLY: '回复'
  }
  return types[type as keyof typeof types] || '未知'
}

function getTargetTypeTag(type: string): 'success' | 'warning' | 'danger' | 'info' | '' {
  const types: Record<string, 'success' | 'warning' | 'danger' | 'info' | ''> = {
    THREAD: '',
    COMMENT: 'success',
    REPLY: 'warning'
  }
  return types[type] || 'info'
}

function getReportType(type: string) {
  const types = {
    SPAM: '垃圾广告',
    PORN: '色情低俗',
    HATE_SPEECH: '仇恨言论',
    COPYRIGHT: '涉嫌侵权',
    DISORDERLY: '扰乱社区秩序',
    VIOLATION: '违法违规',
    OTHER: '其他'
  }
  return types[type as keyof typeof types] || '未知'
}

function getHandleStatus(status: string) {
  const statuses = {
    PENDING: '待处理',
    APPROVED: '已处理-违规',
    REJECTED: '已处理-驳回'
  }
  return statuses[status as keyof typeof statuses] || '未知'
}

function getHandleStatusType(status: string): 'success' | 'warning' | 'danger' | 'info' {
  const types: Record<string, 'success' | 'warning' | 'danger' | 'info'> = {
    PENDING: 'warning',
    APPROVED: 'danger',
    REJECTED: 'info'
  }
  return types[status] || 'info'
}

function openHandleDialog(row: ReportInfo) {
  handleForm.reportId = row.reportId
  handleForm.handleStatus = 'APPROVED'
  handleForm.handleReason = ''
  handleForm.notifyUser = true
  handleDialogVisible.value = true
}

async function submitHandle() {
  handleLoading.value = true
  try {
    await handleReport({
      reportId: handleForm.reportId,
      handleStatus: handleForm.handleStatus,
      handleReason: handleForm.handleReason,
      notifyUser: handleForm.notifyUser
    })
    ElMessage.success('处理成功')
    handleDialogVisible.value = false
    loadReports()
  } finally {
    handleLoading.value = false
  }
}

onMounted(() => {
  loadReports()
})
</script>
<style lang="scss" scoped>
.pager-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
