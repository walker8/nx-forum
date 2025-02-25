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
          <el-form-item prop="ipAddress" label="操作IP">
            <el-input v-model.trim="searchData.ipAddress" placeholder="请输入操作IP" />
          </el-form-item>
          <el-form-item prop="logType" label="日志类型">
            <el-select
              v-model="searchData.logType"
              placeholder="请选择日志类型"
              style="width: 200px"
            >
              <el-option label="全部" :value="0" />
              <el-option label="登录" :value="1" />
              <el-option label="登出" :value="2" />
              <el-option label="注册" :value="3" />
              <el-option label="信息修改" :value="4" />
              <el-option label="密码修改" :value="5" />
            </el-select>
          </el-form-item>
          <div class="el-form-item">
            <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
          </div>
        </el-form>
      </div>
      <div class="toolbar-wrapper">
        <div></div>
        <div>
          <el-tooltip content="刷新当前页">
            <el-button
              type="primary"
              :icon="RefreshRight"
              circle
              @click="getTableData"
              size="small"
            />
          </el-tooltip>
        </div>
      </div>
      <div class="table-wrapper">
        <el-table :data="tableData" v-loading="loading" border>
          <el-table-column prop="userName" label="用户名" align="center" width="150" />
          <el-table-column prop="logTypeDesc" label="日志类型" align="center" width="120" />
          <el-table-column
            prop="logContent"
            label="日志内容"
            min-width="250"
            show-overflow-tooltip
          />
          <el-table-column prop="ipAddress" label="用户IP" align="center" width="150">
            <template #default="scope">
              <el-tooltip
                class="box-item"
                effect="dark"
                :content="scope.row.ipAddress"
                placement="top-start"
              >
                <el-text>{{ scope.row.location }}</el-text>
              </el-tooltip>
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
          <el-table-column prop="operationStatusDesc" label="操作状态" align="center" width="100">
            <template #default="scope">
              <el-tag type="success" v-if="scope.row.operationStatus === 0">成功</el-tag>
              <el-tag type="danger" v-else>失败</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="操作时间" align="center" width="180" />
        </el-table>
      </div>
      <div class="pager-wrapper">
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
  </div>
</template>
<script setup lang="ts">
import { Refresh, RefreshRight, Search } from '@element-plus/icons-vue'
import { type FormInstance, ElMessage } from 'element-plus'
import { queryUserLogs, type UserLogVO } from '~/apis/uc/log'
import { queryUsername } from '~/apis/uc/user'
import type { UserVO } from '~/types/global'

definePageMeta({
  layout: 'uc'
})

const searchData = reactive({
  userId: undefined as number | undefined,
  userName: '',
  ipAddress: '',
  logType: 0
})
const tableData = ref<UserLogVO[]>([])
const searchFormRef = ref<FormInstance>()
const loading = ref<boolean>(false)
const { paginationData, handleCurrentChange, handleSizeChange } = usePagination()

// 获取表格数据
const getTableData = () => {
  loading.value = true
  queryUserLogs({
    pageNo: paginationData.currentPage,
    pageSize: paginationData.pageSize,
    userId: searchData.userId ? Number(searchData.userId) : undefined,
    ipAddress: searchData.ipAddress || undefined,
    logType: searchData.logType > 0 ? searchData.logType : undefined
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

// 处理搜索
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
    return (res.data || []).map((item: UserVO) => ({
      value: item.userName,
      ...item
    })) as Array<UserVO & { value: string }>
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

// 监听分页参数的变化
watch([() => paginationData.currentPage, () => paginationData.pageSize], getTableData, {
  immediate: true
})
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
.search-wrapper {
  min-width: 800px;
}
</style>
