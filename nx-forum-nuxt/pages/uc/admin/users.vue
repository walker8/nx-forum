<template>
  <div class="nx-container">
    <el-card shadow="never">
      <div class="search-wrapper">
        <el-form ref="searchFormRef" :inline="true" :model="searchData" label-width="auto">
          <el-form-item prop="userName" label="用户名">
            <el-input v-model.trim="searchData.userName" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item prop="ip" label="登录IP">
            <el-input v-model.trim="searchData.loginIp" placeholder="请输入登录IP" />
          </el-form-item>
          <el-form-item prop="orderBy" label="排序">
            <el-select v-model="searchData.orderBy" placeholder="请选择排序" style="width: 200px">
              <el-option label="创建时间" value="user_id" />
              <el-option label="更新时间" value="update_time" />
              <el-option label="最后活跃时间" value="last_active_date" />
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
          <el-button type="success" :icon="Plus" size="default" @click="addUser">
            增加用户
          </el-button>
          <el-button type="danger" :icon="Delete" size="default" @click="deleteUsers">
            删除用户
          </el-button>
        </div>
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
        <el-table :data="tableData" v-loading="loading" border ref="tableRef">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column prop="userId" label="用户ID" align="center" width="120" />
          <el-table-column prop="userName" label="用户名" align="center" width="180">
            <template #default="scope">
              <el-space>
                <el-avatar :src="scope.row.avatar" :size="30" v-if="scope.row.avatar" />
                <el-text class="mx-1" :line-clamp="1">{{ scope.row.userName }}</el-text>
              </el-space>
            </template>
          </el-table-column>
          <el-table-column prop="email" label="邮箱地址" min-width="200" align="center" />
          <el-table-column prop="intro" label="个人介绍" show-overflow-tooltip min-width="200" />
          <el-table-column prop="lastActiveIp" label="最后活跃地址" align="center" width="120">
            <template #default="scope">
              <el-tooltip
                class="box-item"
                effect="dark"
                :content="scope.row.lastActiveIp"
                placement="top-start"
              >
                <el-text>{{ scope.row.location }}</el-text>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="账号状态" align="center" width="120">
            <template #default="scope">
              <el-tag type="success" v-if="scope.row.accountStatus == 0">正常</el-tag>
              <el-tag type="warning" v-else-if="scope.row.accountStatus == 1">停用</el-tag>
              <el-tag type="danger" v-else>注销</el-tag>
            </template>
          </el-table-column>
          <el-table-column
            v-if="searchData.orderBy === 'user_id'"
            prop="createTime"
            label="创建时间"
            align="center"
            width="150"
          />
          <el-table-column
            v-if="searchData.orderBy === 'update_time'"
            prop="updateTime"
            label="更新时间"
            align="center"
            width="150"
          />
          <el-table-column
            v-if="searchData.orderBy === 'last_active_date'"
            prop="lastActiveDate"
            label="最后活跃时间"
            align="center"
            width="150"
          />
          <el-table-column fixed="right" label="操作" width="150" align="center">
            <template #default="scope">
              <el-button type="primary" text bg size="small" @click="updateUser(scope.row)">
                修改
              </el-button>
              <el-button
                type="danger"
                text
                bg
                size="small"
                @click="deleteUser(scope.row.userId)"
                :disabled="scope.row.userId <= 1"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
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
    <uc-dialog ref="dialogRef" @refresh="handleSearch" />
  </div>
</template>
<script setup lang="ts">
import { Delete, Plus, Refresh, RefreshRight, Search } from '@element-plus/icons-vue'
import { type FormInstance, ElMessage, ElMessageBox } from 'element-plus'
import { deleteUserByAdmin, deleteUsersByAdmin, queryUsersByAdmin } from '~/apis/uc/user'

definePageMeta({
  layout: 'uc'
})
const searchData = reactive({
  userName: '',
  loginIp: '',
  orderBy: 'user_id'
})
const tableData = ref([])
const searchFormRef = ref<FormInstance>()
const loading = ref<boolean>(false)
const { paginationData, handleCurrentChange, handleSizeChange } = usePagination()
const tableRef = ref()
const dialogRef = ref()

const addUser = () => {
  dialogRef.value?.showDialog(null)
}
const updateUser = (user: any) => {
  dialogRef.value?.showDialog(user)
}
const deleteUser = (userId: number) => {
  ElMessageBox.confirm('确定删除该用户吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      deleteUserByAdmin(userId)
        .then(() => {
          ElMessage.success('删除用户成功')
          getTableData()
        })
        .catch((errMsg) => {
          ElMessage.error(errMsg)
        })
    })
    .catch(() => {})
}
const deleteUsers = () => {
  const users = tableRef.value?.getSelectionRows() || []
  if (users.length === 0) {
    ElMessage.warning('请选择要删除的用户')
  } else {
    ElMessageBox.confirm('确定删除选中的用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      deleteUsersByAdmin(users.map((user: any) => user.userId)).then(() => {
        ElMessage.success('删除用户成功')
        getTableData()
      })
    })
  }
}

const getTableData = () => {
  loading.value = true
  queryUsersByAdmin({
    pageNo: paginationData.currentPage,
    pageSize: paginationData.pageSize,
    orderBy: searchData.orderBy || undefined,
    userName: searchData.userName || undefined,
    loginIp: searchData.loginIp || undefined
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
//#endregion

/** 监听分页参数的变化 */
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
