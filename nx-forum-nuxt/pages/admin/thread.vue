<template>
  <div class="nx-container">
    <el-card shadow="never">
      <div class="search-wrapper">
        <el-form ref="searchFormRef" :inline="true" :model="searchData" label-width="auto">
          <el-form-item prop="keyword" label="关键词">
            <el-input v-model.trim="searchData.keyword" placeholder="请输入关键词" />
          </el-form-item>
          <el-form-item prop="authorName" label="用户名">
            <el-input v-model.trim="searchData.authorName" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item prop="ip" label="ip">
            <el-input v-model.trim="searchData.ip" placeholder="请输入IP" />
          </el-form-item>
          <el-form-item prop="forumId" label="版块">
            <el-select v-model="searchData.forumId" placeholder="请选择版块" style="width: 220px" filterable clearable
              :disabled="forumId !== undefined && forumId > 0">
              <el-option v-for="item in forumMenus" :key="item.forumId" :label="item.nickName" :value="item.forumId">
                <span style="float: left">{{ item.nickName }}</span>
                <span style="float: right; color: var(--el-text-color-secondary); font-size: 13px">
                  {{ item.name }}
                </span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item prop="propertyType" label="类型">
            <el-select v-model="searchData.propertyType" placeholder="请选择类型" style="width: 220px" filterable clearable>
              <el-option v-for="item in propertyTypes" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item prop="orderBy" label="排序">
            <el-select v-model="searchData.orderBy" placeholder="请选择排序" style="width: 220px">
              <el-option label="创建时间" value="create_time" />
              <el-option label="更新时间" value="update_time" />
              <el-option label="最后回复时间" value="last_comment_time" />
            </el-select>
          </el-form-item>
          <el-form-item prop="status" label="状态">
            <el-select v-model="searchData.status" placeholder="请选择状态" style="width: 220px">
              <el-option label="已通过" value="passed" />
              <el-option label="审核中" value="auditing" />
              <el-option label="回收站" value="recycle" />
            </el-select>
          </el-form-item>
          <div class="el-form-item">
            <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
          </div>
        </el-form>
      </div>
      <div class="toolbar-wrapper">
        <div>
          <el-button-group v-if="deleted">
            <el-button type="primary" size="default" @click="restoreBatch()"
              v-if="hasPermission('admin:thread:restore', forumId)">
              还原
            </el-button>
          </el-button-group>
          <el-button-group v-else-if="auditStatus === AuditStatus.AUDITING">
            <el-button type="success" size="default" @click="passBatch()"
              v-if="hasPermission('admin:thread:pass', forumId)">
              通过
            </el-button>
            <el-button type="danger" size="default" @click="rejectBatch()"
              v-if="hasPermission('admin:thread:reject', forumId)">
              拒绝
            </el-button>
            <el-button type="info" size="default" @click="transferBatch()"
              v-if="hasPermission('admin:thread:transfer', forumId)">
              转移
            </el-button>
          </el-button-group>
          <el-button-group v-else>
            <el-button type="danger" size="default" @click="deleteBatch()"
              v-if="hasPermission('admin:thread:delete', forumId)">
              删除
            </el-button>
            <el-button type="primary" size="default" @click="topBatch()"
              v-if="hasPermission('admin:thread:top', forumId)">
              置顶
            </el-button>
            <el-button type="warning" size="default" @click="closeBatch()"
              v-if="hasPermission('admin:thread:close', forumId)">
              关闭
            </el-button>
            <el-button type="success" size="default" @click="digestBatch()"
              v-if="hasPermission('admin:thread:digest', forumId)">
              精华
            </el-button>
            <el-button type="primary" size="default" @click="recommendBatch()"
              v-if="hasPermission('admin:thread:recommend', forumId)">
              推荐
            </el-button>
            <el-button type="info" size="default" @click="transferBatch()"
              v-if="hasPermission('admin:thread:transfer', forumId)">
              转移
            </el-button>
            <el-button type="warning" size="default" @click="archiveBatch()"
              v-if="hasPermission('admin:thread:transfer', forumId)">
              归档
            </el-button>
          </el-button-group>
          <el-switch style="margin-left: 12px" v-model="showBrief" active-text="帖子描述" />
        </div>
        <div>
          <el-tooltip content="刷新当前页">
            <el-button type="primary" :icon="RefreshRight" circle @click="getTableData" size="small" />
          </el-tooltip>
        </div>
      </div>
      <div class="table-wrapper">
        <el-table :data="tableData" v-loading="loading" border ref="tableRef">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column prop="subject" label="主题" min-width="550" align="left">
            <template #default="scope">
              <div class="space-x-2">
                <el-text type="primary" v-if="scope.row.imageCount > 0">
                  图{{ scope.row.imageCount }}
                </el-text>
                <el-text type="primary" v-if="scope.row.top == 1">置顶</el-text>
                <el-text type="primary" v-if="scope.row.top == 2">全局置顶</el-text>
                <el-text type="warning" v-if="scope.row.closed">关闭</el-text>
                <el-text type="success" v-if="scope.row.digest">精华</el-text>
                <el-text tag="b" class="hover:cursor-pointer" @click="open(`/t/${scope.row.threadId}`)">
                  {{ scope.row.subject ? scope.row.subject : '无主题' }}
                </el-text>
                <el-text size="small">
                  回帖{{ scope.row.comments }}·浏览{{ scope.row.views }}
                </el-text>
              </div>
              <div v-show="showBrief">
                <el-text size="small">{{ scope.row.brief }}</el-text>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="userIp" label="发帖地址" align="center" width="120">
            <template #default="scope">
              <el-tooltip class="box-item" effect="dark" :content="scope.row.userIp" placement="top-start">
                <el-text>{{ scope.row.location }}</el-text>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column prop="forumNickName" label="版块" align="center" width="120">
            <template #default="scope">
              <el-text class="mx-1">{{ scope.row.forumNickName }}</el-text>
            </template>
          </el-table-column>
          <el-table-column prop="authorName" label="作者" align="center" width="120">
            <template #default="scope">
              <el-text class="mx-1">{{ scope.row.authorName }}</el-text>
            </template>
          </el-table-column>
          <el-table-column prop="client" label="客户端信息" align="center" min-width="200">
            <template #default="scope">
              <el-space wrap>
                <el-tag type="primary" v-if="scope.row.browser">{{ scope.row.browser }}</el-tag>
                <el-tag type="success" v-if="scope.row.os">{{ scope.row.os }}</el-tag>
              </el-space>
            </template>
          </el-table-column>
          <el-table-column prop="auditReason" label="审核原因" align="center" min-width="150"
            v-if="auditStatus === AuditStatus.AUDITING" />
          <el-table-column prop="createTime" label="创建时间" align="center" width="150" />
          <el-table-column prop="updateTime" label="更新时间" align="center" width="150" />
          <el-table-column fixed="right" label="操作" width="180" align="center">
            <template #default="scope">
              <el-button-group v-if="deleted">
                <el-button type="primary" text bg size="small" @click="restoreBatch([scope.row.threadId])"
                  v-if="hasPermission('admin:thread:restore', forumId)">
                  还原
                </el-button>
              </el-button-group>
              <el-button-group v-else-if="auditStatus === AuditStatus.AUDITING">
                <el-button type="success" text bg size="small" @click="passBatch([scope.row.threadId])"
                  v-if="hasPermission('admin:thread:pass', forumId)">
                  通过
                </el-button>
                <el-button type="danger" text bg size="small" @click="rejectBatch([scope.row.threadId])"
                  v-if="hasPermission('admin:thread:reject', forumId)">
                  拒绝
                </el-button>
              </el-button-group>
              <el-button-group v-else>
                <el-button type="primary" text bg size="small" @click="go(`/editor/t/${scope.row.threadId}`)"
                  v-if="hasPermission('admin:thread:edit', forumId)">
                  编辑
                </el-button>
                <el-button type="danger" text bg size="small" @click="deleteBatch([scope.row.threadId])"
                  v-if="hasPermission('admin:thread:delete', forumId)">
                  删除
                </el-button>
              </el-button-group>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pager-wrapper">
        <el-pagination background :layout="paginationData.layout" :page-sizes="paginationData.pageSizes"
          :total="paginationData.total" :page-size="paginationData.pageSize" :currentPage="paginationData.currentPage"
          @size-change="handleSizeChange" @current-change="handleCurrentChange" />
      </div>
    </el-card>
    <!-- 消息框 -->
    <admin-thread-operation-dialog ref="operationDialogRef" @confirm="handleOperation" />
  </div>
</template>
<script setup lang="ts">
import { Refresh, RefreshRight, Search } from '@element-plus/icons-vue'
import { type FormInstance, ElMessage, ElMessageBox } from 'element-plus'
import { getAuditingCount } from '~/apis/admin'
import { getAdminForumMenu } from '~/apis/forum'
import { operateThreadsByAdmin, queryThreadsByAdmin } from '~/apis/thread'
import { AuditStatus } from '~/composables/useAdmin'
import { OperationConstant } from '~/constant'
import type { Thread } from '~/types/global'

definePageMeta({
  layout: 'admin'
})

const route = useRoute()
const forumId = computed<number | undefined>(() => {
  const forumId = route.query.forumId
  return forumId ? Number(forumId) : undefined
})
const { hasPermission } = useUserAuth(forumId.value)
const go = (path: string) => {
  navigateTo(path)
}
const open = (path: string) => {
  window.open(path, '_blank')
}
const searchData = reactive({
  authorName: '',
  forumId: forumId.value,
  keyword: '',
  ip: '',
  propertyType: '',
  orderBy: 'create_time',
  status: ''
})
searchData.status = (route.query.status as string) || 'passed'
const propertyTypes = [
  { label: '置顶', value: 1 },
  { label: '精华', value: 2 },
  { label: '推荐', value: 4 }
]
const showBrief = ref(false)
const tableData = ref([])
const tableRef = ref()
const searchFormRef = ref<FormInstance>()
const operationDialogRef = ref()
const loading = ref<boolean>(false)
const { paginationData, handleCurrentChange, handleSizeChange } = usePagination()

// 用于跟踪当前请求，防止竞态条件
let currentRequestId = 0

// 初始化 auditStatus 和 deleted
const auditStatus = ref<AuditStatus | null>(null)
const deleted = ref(false)
const initAuditStatusAndDeleted = () => {
  const { auditStatus: initAuditStatus, deleted: initDeleted } = useAuditStatus(searchData.status)
  auditStatus.value = initAuditStatus
  deleted.value = initDeleted
}
initAuditStatusAndDeleted()
const getTableData = () => {
  // 生成新的请求ID
  const requestId = ++currentRequestId
  loading.value = true
  queryThreadsByAdmin({
    pageNo: paginationData.currentPage,
    pageSize: paginationData.pageSize,
    authorName: searchData.authorName || undefined,
    keyword: searchData.keyword || undefined,
    ip: searchData.ip || undefined,
    propertyType: Number(searchData.propertyType) || undefined,
    orderBy: searchData.orderBy || undefined,
    forumId: Number(searchData.forumId) || undefined,
    auditStatus: auditStatus.value || undefined,
    deleted: deleted.value || undefined
  })
    .then((res) => {
      // 只有当前请求是最新的请求时才更新数据
      if (requestId === currentRequestId) {
        const data = res.data
        paginationData.total = data.total
        tableData.value = data.records
      }
    })
    .catch((err) => {
      // 只有当前请求是最新的请求时才显示错误
      if (requestId === currentRequestId) {
        ElMessage.error(err)
        tableData.value = []
      }
    })
    .finally(() => {
      // 只有当前请求是最新的请求时才更新loading状态
      if (requestId === currentRequestId) {
        loading.value = false
      }
    })
}

const handleSearch = () => {
  initAuditStatusAndDeleted()

  if (paginationData.currentPage === 1) {
    getTableData()
  } else {
    paginationData.currentPage = 1
  }
}
const resetSearch = () => {
  searchFormRef.value?.resetFields()
  handleSearch()
}
const auditingCount = useAuditingCount()
const rejectBatch = (threadIds?: number[]) => {
  const ids =
    threadIds || tableRef.value?.getSelectionRows()?.map((item: Thread) => item.threadId) || []
  if (ids.length === 0) {
    ElMessage.warning('请选择要拒绝的帖子')
    return
  }
  const reason = ref('')
  const notice = ref(true)
  ElMessageBox({
    title: '拒绝帖子',
    message: () =>
      h('div', null, [
        h(ElInput, {
          class: 'w-full',
          modelValue: reason.value,
          'onUpdate:modelValue': (val: string) => (reason.value = val),
          placeholder: '请输入拒绝原因'
        }),
        h('div', { class: 'mt-3 flex items-center' }, [
          h('span', { class: 'text-sm mr-2' }, '通知用户'),
          h(ElSwitch, {
            size: 'small',
            modelValue: notice.value,
            'onUpdate:modelValue': (val: any) => (notice.value = Boolean(val))
          })
        ])
      ]),
    customClass: 'full-width-message',
    confirmButtonText: '确定'
  }).then(() => {
    handleOperation(OperationConstant.REJECT, ids, reason.value, notice.value, Number(searchData.forumId) || 0)
  })
}

const topBatch = () => {
  const ids = tableRef.value?.getSelectionRows()?.map((item: Thread) => item.threadId) || []
  if (ids.length === 0) {
    ElMessage.warning('请选择要置顶的帖子')
    return
  }
  operationDialogRef?.value.showDialog(OperationConstant.TOP, ids)
}

const closeBatch = () => {
  const ids = tableRef.value?.getSelectionRows()?.map((item: Thread) => item.threadId) || []
  if (ids.length === 0) {
    ElMessage.warning('请选择要关闭的帖子')
    return
  }
  operationDialogRef?.value.showDialog(OperationConstant.CLOSE, ids)
}

const digestBatch = () => {
  const ids = tableRef.value?.getSelectionRows()?.map((item: Thread) => item.threadId) || []
  if (ids.length === 0) {
    ElMessage.warning('请选择要加精的帖子')
    return
  }
  operationDialogRef?.value.showDialog(OperationConstant.DIGEST, ids)
}

const recommendBatch = () => {
  const ids = tableRef.value?.getSelectionRows()?.map((item: Thread) => item.threadId) || []
  if (ids.length === 0) {
    ElMessage.warning('请选择要推荐的帖子')
    return
  }
  operationDialogRef?.value.showDialog(OperationConstant.RECOMMEND, ids)
}

const passBatch = (threadIds?: number[]) => {
  const ids =
    threadIds || tableRef.value?.getSelectionRows()?.map((item: Thread) => item.threadId) || []
  if (ids.length === 0) {
    ElMessage.warning('请选择要通过的帖子')
    return
  }
  const notice = ref(true)
  ElMessageBox({
    title: '通过帖子',
    message: () =>
      h('div', [
        h('div', { class: 'mt-3' }, '确定通过吗？'),
        h('div', { class: 'mt-3 flex items-center' }, [
          h('span', { class: 'text-sm mr-2' }, '通知用户'),
          h(ElSwitch, {
            size: 'small',
            modelValue: notice.value,
            'onUpdate:modelValue': (val: any) => (notice.value = Boolean(val))
          })
        ])
      ]),
    customClass: 'full-width-message',
    confirmButtonText: '确定'
  }).then(() => {
    handleOperation(OperationConstant.PASS, ids, null, notice.value, Number(searchData.forumId) || 0)
  })
}

const deleteBatch = (threadIds?: number[]) => {
  const ids =
    threadIds || tableRef.value?.getSelectionRows()?.map((item: Thread) => item.threadId) || []
  if (ids.length === 0) {
    ElMessage.warning('请选择要删除的帖子')
    return
  }
  const reason = ref('')
  const notice = ref(true)
  ElMessageBox({
    title: '删除帖子',
    message: () =>
      h('div', null, [
        h(ElInput, {
          class: 'w-full',
          modelValue: reason.value,
          'onUpdate:modelValue': (val: string) => (reason.value = val),
          placeholder: '请输入删除原因'
        }),
        h('div', { class: 'mt-3 flex items-center' }, [
          h('span', { class: 'text-sm mr-2' }, '通知用户'),
          h(ElSwitch, {
            size: 'small',
            modelValue: notice.value,
            'onUpdate:modelValue': (val: any) => (notice.value = Boolean(val))
          })
        ])
      ]),
    customClass: 'full-width-message',
    confirmButtonText: '确定'
  }).then(() => {
    handleOperation(OperationConstant.DELETE, ids, reason.value, notice.value, Number(searchData.forumId) || 0)
  })
}
const restoreBatch = (threadIds?: number[]) => {
  const ids =
    threadIds || tableRef.value?.getSelectionRows()?.map((item: Thread) => item.threadId) || []
  if (ids.length === 0) {
    ElMessage.warning('请选择要恢复的帖子')
    return
  }
  const notice = ref(true)
  ElMessageBox({
    title: '恢复帖子',
    message: () =>
      h('div', [
        h('div', { style: 'margin-bottom: 15px' }, '确定还原吗？'),
        h('div', { class: 'mt-3 flex items-center' }, [
          h('span', { class: 'text-sm mr-2' }, '通知用户:'),
          h(ElSwitch, {
            size: 'small',
            modelValue: notice.value,
            'onUpdate:modelValue': (val: any) => (notice.value = Boolean(val))
          })
        ])
      ]),
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => {
    handleOperation(OperationConstant.RESTORE, ids, null, notice.value, Number(searchData.forumId) || 0)
  })
}

const handleOperation = (
  operation: string,
  threadIds: number[],
  reason: string | null = null,
  notice: boolean = true,
  forumId: number = 0
) => {
  operateThreadsByAdmin(threadIds, forumId, operation, reason, notice)
    .then((res) => {
      ElMessage.success(res.data)
      if (operation === OperationConstant.PASS || operation === OperationConstant.REJECT) {
        getAuditingCount(Number(searchData.forumId) || 0).then((res) => {
          auditingCount.value = res.data
        })
      }
      getTableData()
    })
    .catch((errMsg) => {
      ElMessage.error(errMsg)
    })
}

/** 监听分页参数的变化 */
watch([() => paginationData.currentPage, () => paginationData.pageSize], getTableData, {
  immediate: true
})

const forumMenus = useUserMenus()
if (forumMenus.value.length === 0) {
  getAdminForumMenu().then((res) => {
    const data = res.data
    forumMenus.value = data
  })
}

const transferBatch = () => {
  const ids = tableRef.value?.getSelectionRows()?.map((item: Thread) => item.threadId) || []
  if (ids.length === 0) {
    ElMessage.warning('请选择要转移的帖子')
    return
  }

  let targetForumId = ref('')
  ElMessageBox({
    title: '转移帖子',
    message: () =>
      h('div', [
        h('div', { style: 'margin-bottom: 15px' }, '请选择目标版块：'),
        h(
          ElSelect,
          {
            modelValue: targetForumId.value,
            'onUpdate:modelValue': (val) => (targetForumId.value = val),
            placeholder: '请选择版块',
            style: 'width: 100%',
            filterable: true,
            clearable: true
          },
          forumMenus.value.map((forum) =>
            h(ElOption, {
              key: forum.forumId,
              label: forum.nickName,
              value: forum.forumId
            })
          )
        )
      ]),
    customClass: 'full-width-message',
    confirmButtonText: '确定'
  })
    .then(() => {
      if (!targetForumId.value) {
        ElMessage.warning('请选择目标版块')
        return
      }
      handleOperation(OperationConstant.TRANSFER, ids, null, true, Number(targetForumId.value))
    })
    .catch(() => { })
}

const archiveBatch = () => {
  const ids = tableRef.value?.getSelectionRows()?.map((item: Thread) => item.threadId) || []
  if (ids.length === 0) {
    ElMessage.warning('请选择要归档的帖子')
    return
  }

  ElMessageBox.confirm('归档后的帖子将不会在最新、推荐、精华、热门版块中显示。', '归档确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      // 归档就是转移到归档版块（id=6）
      handleOperation(OperationConstant.TRANSFER, ids, null, false, 6)
    })
    .catch(() => { })
}
</script>

<style lang="scss" scoped>
.toolbar-wrapper {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.pager-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
