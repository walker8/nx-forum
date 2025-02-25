<template>
  <div class="nx-container">
    <el-card shadow="never">
      <div class="search-wrapper">
        <el-form ref="searchFormRef" :inline="true" :model="searchData" label-width="auto">
          <el-form-item prop="userName" label="用户名">
            <el-autocomplete
              v-model="searchData.userName"
              :fetch-suggestions="querySearchUser"
              placeholder="请输入用户名"
              @select="(item) => handleSearchUserSelect(item as UserVO)"
              value-key="userName"
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item prop="roleKey" label="角色编码">
            <el-input v-model.trim="searchData.roleKey" placeholder="请输入角色编码" />
          </el-form-item>
          <div class="el-form-item">
            <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
          </div>
        </el-form>
      </div>

      <div class="toolbar-wrapper">
        <div>
          <el-tooltip content="刷新">
            <el-button type="primary" :icon="RefreshRight" circle @click="getTableData" />
          </el-tooltip>
        </div>
      </div>

      <div class="table-wrapper">
        <el-table :data="tableData" v-loading="loading" border>
          <el-table-column prop="userId" label="用户ID" width="100" align="center" />
          <el-table-column prop="userName" label="用户名" min-width="120" align="center" />
          <el-table-column prop="roleKey" label="角色编码" min-width="120" align="center" />
          <el-table-column prop="roleName" label="角色名称" min-width="120" align="center" />
          <el-table-column prop="roleScope" label="角色范围" width="120" align="center">
            <template #default="scope">
              <el-tag type="info">
                {{ scope.row.roleScope }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="授权时间" width="180" align="center" />
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template #default="scope">
              <el-button
                type="danger"
                text
                @click="cancelUserRole(scope.row.id)"
                :disabled="scope.row.userId === 1"
              >
                取消授权
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="pager-wrapper">
        <el-pagination
          v-model:current-page="paginationData.currentPage"
          v-model:page-size="paginationData.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="paginationData.total"
          layout="total, sizes, prev, pager, next, jumper"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { Search, Refresh, RefreshRight } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { queryUserRoles, cancelUserRole as cancelUserRoleApi } from '~/apis/uc/role'
import { queryUsername } from '~/apis/uc/user'
import type { UserVO } from '~/types/global'

definePageMeta({
  layout: 'uc'
})

// 表格数据
const loading = ref(false)
const tableData = ref([])

// 搜索数据
const searchFormRef = ref()
const searchData = reactive({
  userName: '',
  userId: undefined as number | undefined,
  roleKey: ''
})

// 分页数据
const paginationData = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 获取表格数据
const getTableData = async () => {
  loading.value = true
  try {
    const res = await queryUserRoles({
      pageNo: paginationData.currentPage,
      pageSize: paginationData.pageSize,
      userId: searchData.userId,
      userName: searchData.userName || undefined,
      roleKey: searchData.roleKey || undefined
    })
    const data = res.data
    paginationData.total = data.total
    tableData.value = data.records
  } catch (err: any) {
    ElMessage.error(err.message || '获取用户角色列表失败')
    tableData.value = []
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  if (paginationData.currentPage === 1) {
    getTableData()
  } else {
    paginationData.currentPage = 1
  }
}

// 用户名搜索建议
const querySearchUser = async (query: string) => {
  if (!query) return []
  try {
    const res = await queryUsername(query)
    return (res.data || []).map(item => ({
      value: item.userName,
      ...item
    }))
  } catch (error) {
    return []
  }
}

// 用户选择处理
const handleSearchUserSelect = (item: UserVO) => {
  searchData.userId = item.userId
  searchData.userName = item.userName
}

// 重置搜索
const resetSearch = () => {
  searchFormRef.value?.resetFields()
  searchData.userId = undefined
  searchData.userName = ''
  handleSearch()
}

// 取消用户角色
const cancelUserRole = (id: number) => {
  ElMessageBox.confirm('确认取消该用户的角色授权吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await cancelUserRoleApi(id)
      ElMessage.success('取消授权成功')
      getTableData()
    } catch (err: any) {
      ElMessage.error(err.message || '取消授权失败')
    }
  })
}

/** 监听分页参数的变化 */
watch([() => paginationData.currentPage, () => paginationData.pageSize], getTableData, {
  immediate: true
})
</script>

<style lang="scss" scoped>
.search-wrapper {
  margin-bottom: 20px;
}
.toolbar-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}
.pager-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
:deep(.el-button-group) {
  .el-button {
    padding-left: 5px;
    padding-right: 5px;
    & + .el-button {
      margin-left: -1px;
    }
  }
}
</style> 