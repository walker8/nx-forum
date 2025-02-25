<template>
  <div class="nx-container">
    <el-card shadow="never">
      <div class="search-wrapper">
        <el-form ref="searchFormRef" :inline="true" :model="searchData" label-width="auto">
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
              v-if="hasPermission('admin:comment:restore', forumId)">
              批量还原
            </el-button>
          </el-button-group>
          <el-button-group v-else-if="auditStatus === AuditStatus.AUDITING">
            <el-button type="success" size="default" @click="passBatch()"
              v-if="hasPermission('admin:comment:pass', forumId)">
              批量通过
            </el-button>
            <el-button type="danger" size="default" @click="rejectBatch()"
              v-if="hasPermission('admin:comment:reject', forumId)">
              批量拒绝
            </el-button>
          </el-button-group>
          <el-button-group v-else>
            <el-button type="danger" size="default" @click="deleteBatch()"
              v-if="hasPermission('admin:comment:delete', forumId)">
              批量删除
            </el-button>
          </el-button-group>
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
          <el-table-column prop="message" label="回帖内容" min-width="500" align="left">
            <template #default="scope">
              <el-space wrap class="hover:cursor-pointer" @click="toComment(scope.row)">
                <el-text line-clamp="2">{{ scope.row.message }}</el-text>
                <el-text type="success" v-if="scope.row.images?.length > 0">
                  图{{ scope.row.images?.length }}
                </el-text>
                <el-text type="warning" v-if="scope.row.replyCount > 0">
                  评论{{ scope.row.replyCount }}
                </el-text>
              </el-space>
            </template>
          </el-table-column>
          <el-table-column prop="userIp" label="回帖地址" align="center" width="120">
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
          <el-table-column prop="auditReason" label="审核原因" align="center" min-width="150"
            v-if="auditStatus === AuditStatus.AUDITING" />
          <el-table-column prop="client" label="客户端信息" align="center" min-width="200">
            <template #default="scope">
              <el-space wrap>
                <el-tag type="primary" v-if="scope.row.browser">{{ scope.row.browser }}</el-tag>
                <el-tag type="success" v-if="scope.row.os">{{ scope.row.os }}</el-tag>
              </el-space>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" align="center" width="150" />
          <el-table-column fixed="right" label="操作" width="180" align="center">
            <template #default="scope">
              <el-button-group v-if="deleted">
                <el-button type="primary" text bg size="small" @click="restoreBatch([scope.row.commentId])"
                  v-if="hasPermission('admin:comment:restore', forumId)">
                  还原
                </el-button>
              </el-button-group>
              <el-button-group v-else-if="auditStatus === AuditStatus.AUDITING">
                <el-button type="success" text bg size="small" @click="passBatch([scope.row.commentId])"
                  v-if="hasPermission('admin:comment:pass', forumId)">
                  通过
                </el-button>
                <el-button type="danger" text bg size="small" @click="rejectBatch([scope.row.commentId])"
                  v-if="hasPermission('admin:comment:reject', forumId)">
                  拒绝
                </el-button>
              </el-button-group>
              <el-button-group v-else>
                <el-button type="danger" text bg size="small" @click="deleteBatch([scope.row.commentId])"
                  v-if="hasPermission('admin:comment:delete', forumId)">
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
  </div>
</template>
<script setup lang="ts">
import { Refresh, RefreshRight, Search } from '@element-plus/icons-vue'
import { type FormInstance, ElMessage, ElMessageBox } from 'element-plus'
import {
  deleteCommentsByAdmin,
  passCommentsByAdmin,
  queryCommentsByAdmin,
  rejectCommentsByAdmin,
  restoreCommentsByAdmin
} from '~/apis/comment'
import { getAdminForumMenu } from '~/apis/forum'
import type { ForumMenuItemVO } from '~/types/global'
import { AuditStatus } from '~/composables/useAdmin'
import { getAuditingCount } from '~/apis/admin'

definePageMeta({
  layout: 'admin'
})
const route = useRoute()
const forumId = computed<number | undefined>(() => {
  const forumId = route.query.forumId
  return forumId ? Number(forumId) : undefined
})
const { hasPermission } = useUserAuth(forumId.value)
const searchData = reactive({
  authorName: '',
  forumId: forumId.value,
  ip: '',
  status: ''
})
searchData.status = (route.query.status as string) || 'passed'
const tableData = ref([])
const searchFormRef = ref<FormInstance>()
const tableRef = ref()
const loading = ref<boolean>(false)
const { paginationData, handleCurrentChange, handleSizeChange } = usePagination()

// 初始化 auditStatus 和 deleted
const auditStatus = ref<AuditStatus | null>(null)
const deleted = ref(false)
const initAuditStatusAndDeleted = () => {
  const { auditStatus: initAuditStatus, deleted: initDeleted } = useAuditStatus(searchData.status)
  auditStatus.value = initAuditStatus
  deleted.value = initDeleted
}
initAuditStatusAndDeleted()

const toComment = (row: any) => {
  window.open(`/t/${row.threadId}?commentId=${row.commentId}`)
}

const getTableData = () => {
  loading.value = true
  queryCommentsByAdmin({
    pageNo: paginationData.currentPage,
    pageSize: paginationData.pageSize,
    authorName: searchData.authorName || undefined,
    ip: searchData.ip || undefined,
    forumId: Number(searchData.forumId) || undefined,
    auditStatus: auditStatus.value || undefined,
    deleted: deleted.value || undefined
  })
    .then((res) => {
      const data = res.data
      paginationData.total = data.total
      tableData.value = data.records
    })
    .catch((err) => {
      ElMessage.error(err)
      tableData.value = []
    })
    .finally(() => {
      loading.value = false
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
const rejectBatch = (commentIds?: number[]) => {
  const ids =
    commentIds || tableRef.value?.getSelectionRows()?.map((item: any) => item.commentId) || []
  if (ids.length === 0) {
    ElMessage.warning('请选择要拒绝的回复')
    return
  }
  const reason = ref('')
  const notice = ref(true)
  ElMessageBox({
    title: '拒绝评论',
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
    rejectCommentsByAdmin(ids, Number(searchData.forumId) || 0, reason.value, notice.value).then(() => {
      ElMessage.success('拒绝成功')
      getAuditingCount(Number(searchData.forumId) || 0).then((res) => {
        auditingCount.value = res.data
      })
      getTableData()
    })
  })
}

const passBatch = (commentIds?: number[]) => {
  const ids =
    commentIds || tableRef.value?.getSelectionRows()?.map((item: any) => item.commentId) || []
  if (ids.length === 0) {
    ElMessage.warning('请选择要通过的回复')
    return
  }
  const notice = ref(true)
  ElMessageBox({
    title: '通过评论',
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
    passCommentsByAdmin(ids, Number(searchData.forumId) || 0, notice.value).then(() => {
      ElMessage.success('通过成功')
      getAuditingCount(Number(searchData.forumId) || 0).then((res) => {
        auditingCount.value = res.data
      })
      getTableData()
    })
  })
}

const deleteBatch = (commentIds?: number[]) => {
  const ids =
    commentIds || tableRef.value?.getSelectionRows()?.map((item: any) => item.commentId) || []
  if (ids.length === 0) {
    ElMessage.warning('请选择要删除的回复')
    return
  }
  const reason = ref('')
  const notice = ref(true)
  ElMessageBox({
    title: '删除评论',
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
    deleteCommentsByAdmin(ids, Number(searchData.forumId) || 0, reason.value, notice.value).then(() => {
      ElMessage.success('删除成功')
      getTableData()
    })
  })
}
const restoreBatch = (commentIds?: number[]) => {
  const ids =
    commentIds || tableRef.value?.getSelectionRows()?.map((item: any) => item.commentId) || []
  if (ids.length === 0) {
    ElMessage.warning('请选择要恢复的回复')
    return
  }
  const notice = ref(true)
  ElMessageBox({
    title: '恢复评论',
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
    restoreCommentsByAdmin(ids, Number(searchData.forumId) || 0, notice.value).then(() => {
      ElMessage.success('还原成功')
      getTableData()
    })
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
