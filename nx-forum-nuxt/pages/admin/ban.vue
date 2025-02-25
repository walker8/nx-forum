<template>
  <div class="nx-container">
    <el-card shadow="never">
      <div class="search-wrapper">
        <el-form ref="searchFormRef" :inline="true" :model="searchData" label-width="auto">
          <el-form-item prop="userName" label="用户">
            <el-autocomplete v-model="searchData.userName" :fetch-suggestions="querySearchUser" placeholder="请输入用户名"
              @select="(item) => handleSearchUserSelect(item as UserVO)" value-key="userName" style="width: 200px" />
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
          <div class="el-form-item">
            <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
          </div>
        </el-form>
      </div>
      <div class="toolbar-wrapper">
        <div>
          <el-button type="primary" size="default" @click="showBanDialog"
            v-if="hasPermission('admin:user:ban', forumId)">
            添加禁言
          </el-button>
        </div>
        <div>
          <el-tooltip content="刷新当前页">
            <el-button type="primary" :icon="RefreshRight" circle @click="getTableData" size="small" />
          </el-tooltip>
        </div>
      </div>
      <div class="table-wrapper">
        <el-table :data="tableData" v-loading="loading" border ref="tableRef">
          <el-table-column prop="userName" label="用户名" align="center" width="120" />
          <el-table-column prop="forumName" label="版块" align="center" width="120">
            <template #default="scope">
              {{ scope.row.forumName || '全局禁言' }}
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="禁言原因" min-width="200" />
          <el-table-column prop="operationType" label="操作类型" min-width="120">
            <template #default="scope">
              <el-tag :type="scope.row.operationType === 1 ? 'success' : 'danger'">
                {{ scope.row.operationType === 0 ? '禁言' : '解禁' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="operationUserName" label="操作者" min-width="120" />
          <el-table-column prop="createTime" label="操作时间" align="center" width="150" />
          <el-table-column prop="expireTime" label="过期时间" align="center" width="160">
            <template #default="scope">
              <el-tag :type="scope.row.expired ? 'info' : 'danger'" v-if="scope.row.operationType === 0">
                {{ scope.row.expireTime || '永久禁言' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="120" align="center">
            <template #default="scope">
              <el-button v-if="!scope.row.expired && scope.row.operationType === 0" type="danger" text bg size="small"
                @click="handleUnban(scope.row)">
                解除
              </el-button>
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

    <!-- 禁言对话框 -->
    <el-dialog v-model="banDialogVisible" title="用户禁言" width="500px">
      <el-form :model="banForm" :rules="rules" ref="banFormRef" label-width="100px">
        <el-form-item label="用户" prop="userName">
          <el-autocomplete v-model="banForm.userName" :fetch-suggestions="queryUserSearch" placeholder="请输入用户名"
            @select="(item) => handleUserSelect(item as UserVO)" value-key="userName" style="width: 100%" />
        </el-form-item>
        <el-form-item label="版块">
          <el-select v-model="banForm.forumId" placeholder="请选择版块" style="width: 100%" filterable clearable
            :disabled="forumId !== undefined && forumId > 0">
            <el-option v-for="item in forumMenus" :key="item.forumId" :label="item.nickName" :value="item.forumId" />
          </el-select>
        </el-form-item>
        <el-form-item label="禁言原因" prop="reason">
          <el-input v-model="banForm.reason" type="textarea" :rows="3" placeholder="请输入禁言原因" />
        </el-form-item>
        <el-form-item label="禁言时长">
          <el-date-picker v-model="banForm.expireTime" type="datetime" style="width: 100%"
            placeholder="选择禁言截止时间，留空为永久禁言" :disabledDate="disabledDate" :shortcuts="banTimeShortcuts"
            format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="banDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBan">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { Refresh, RefreshRight, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { queryBans, createBan, unbanUser } from '~/apis/ban'
import { getAdminForumMenu } from '~/apis/forum'
import type { UserVO } from '~/types/global'
import { queryUsername } from '~/apis/uc/user'

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
  userId: undefined as number | undefined,
  userName: '',
  forumId: forumId.value
})

const tableData = ref([])
const searchFormRef = ref()
const loading = ref(false)
const { paginationData, handleCurrentChange, handleSizeChange } = usePagination()

// 获取禁言记录
const getTableData = () => {
  loading.value = true
  queryBans({
    pageNo: paginationData.currentPage,
    pageSize: paginationData.pageSize,
    userId: searchData.userId,
    forumId: searchData.forumId
  })
    .then((res) => {
      tableData.value = res.data.records
      paginationData.total = res.data.total
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
  paginationData.currentPage = 1
  getTableData()
}

const resetSearch = () => {
  searchFormRef.value?.resetFields()
  searchData.userId = undefined
  searchData.userName = ''
  handleSearch()
}

// 禁言相关
const banDialogVisible = ref(false)
const banFormRef = ref()
const banForm = reactive({
  userName: '',
  userId: undefined as number | undefined,
  forumId: forumId.value,
  reason: '',
  expireTime: undefined
})

const rules = {
  userName: [{ required: true, message: '请选择用户', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入禁言原因', trigger: 'blur' }]
}

const showBanDialog = () => {
  banForm.userId = undefined
  banForm.forumId = forumId.value
  banForm.reason = ''
  banForm.userName = ''
  banForm.expireTime = undefined
  banDialogVisible.value = true
}

const disabledDate = (time: Date) => {
  return time.getTime() < Date.now()
}

const queryUserSearch = async (query: string) => {
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

const handleUserSelect = (item: UserVO) => {
  banForm.userId = item.userId
  banForm.userName = item.userName
}

const submitBan = () => {
  banFormRef.value?.validate((valid: boolean) => {
    if (valid) {
      if (!banForm.userId) {
        ElMessage.error('请选择有效用户')
        return
      }
      createBan({
        userId: banForm.userId,
        forumId: banForm.forumId || 0,
        reason: banForm.reason,
        expireTime: banForm.expireTime
      })
        .then(() => {
          ElMessage.success('禁言操作成功')
          banDialogVisible.value = false
          getTableData()
        })
        .catch((err) => ElMessage.error(err))
    }
  })
}

// 解除禁言
const handleUnban = (row: any) => {
  ElMessageBox.confirm(`确定要解除 ${row.userName} 的禁言吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      unbanUser({
        userId: row.userId,
        forumId: row.forumId || 0,
        reason: ''
      })
        .then(() => {
          ElMessage.success('解除禁言成功')
          getTableData()
        })
        .catch((err) => ElMessage.error(err))
    })
    .catch(() => { })
}

// 版块菜单
const forumMenus = useUserMenus()
if (forumMenus.value.length === 0) {
  getAdminForumMenu().then((res) => {
    forumMenus.value = res.data
  })
}

// 监听分页变化
watch([() => paginationData.currentPage, () => paginationData.pageSize], getTableData, {
  immediate: true
})

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
}

// 添加禁言时间快捷选项
const banTimeShortcuts = [
  {
    text: '一天',
    value: () => {
      const date = new Date()
      date.setDate(date.getDate() + 1)
      return date
    }
  },
  {
    text: '三天',
    value: () => {
      const date = new Date()
      date.setDate(date.getDate() + 3)
      return date
    }
  },
  {
    text: '一周',
    value: () => {
      const date = new Date()
      date.setDate(date.getDate() + 7)
      return date
    }
  },
  {
    text: '两周',
    value: () => {
      const date = new Date()
      date.setDate(date.getDate() + 14)
      return date
    }
  },
  {
    text: '一个月',
    value: () => {
      const date = new Date()
      date.setMonth(date.getMonth() + 1)
      return date
    }
  },
  {
    text: '三个月',
    value: () => {
      const date = new Date()
      date.setMonth(date.getMonth() + 3)
      return date
    }
  },
  {
    text: '一年',
    value: () => {
      const date = new Date()
      date.setFullYear(date.getFullYear() + 1)
      return date
    }
  },
  {
    text: '两年',
    value: () => {
      const date = new Date()
      date.setFullYear(date.getFullYear() + 2)
      return date
    }
  }
]
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

:deep(.el-input-number) {
  width: 200px;
}
</style>
