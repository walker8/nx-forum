<template>
  <el-dialog v-model="dialogVisible" :title="`角色授权 - ${roleName}`" width="800px">
    <div class="search-wrapper">
      <el-form :inline="true" :model="searchData">
        <el-form-item label="用户名">
          <el-input v-model.trim="searchData.userName" placeholder="请输入用户名" />
        </el-form-item>
        <div class="el-form-item">
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Plus" type="success" @click="showAddUserDialog">新增授权</el-button>
        </div>
      </el-form>
    </div>

    <div class="table-wrapper">
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="userName" label="用户名" min-width="120" align="center" />
        <el-table-column prop="roleScope" label="角色范围" min-width="120" align="center" />
        <el-table-column prop="expireTime" label="过期时间" min-width="150" align="center" />
        <el-table-column prop="createUserName" label="创建用户" min-width="120" align="center" />
        <el-table-column prop="createTime" label="创建时间" min-width="150" align="center" />
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="scope">
            <el-button type="danger" text @click="cancelUserRole(scope.row)"> 取消授权 </el-button>
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

    <!-- 新增用户授权对话框 -->
    <el-dialog v-model="addUserDialogVisible" title="新增用户授权" width="500px" append-to-body>
      <el-form :model="addUserForm" label-width="80px">
        <el-form-item label="用户" prop="userId">
          <el-select
            v-model="addUserForm.userId"
            filterable
            remote
            reserve-keyword
            placeholder="请输入用户名搜索"
            :remote-method="handleUserSearch"
            :loading="userSearchLoading"
            style="width: 100%"
          >
            <el-option
              v-for="item in userOptions"
              :key="item.userId"
              :label="item.userName"
              :value="item.userId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="角色范围" prop="roleScope">
          <el-input v-model="addUserForm.roleScope" placeholder="请输入角色范围" clearable />
        </el-form-item>
        <el-form-item label="过期时间" prop="expireTime">
          <el-date-picker
            v-model="addUserForm.expireTime"
            type="datetime"
            placeholder="请选择过期时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            :shortcuts="dateShortcuts"
            style="width: 100%"
            clearable
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addUserDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddUser">确定</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup lang="ts">
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { queryUserRoles, assignUserRole, cancelUserRole as cancelUserRoleApi } from '~/apis/uc/role'
import { queryUsersByAdmin } from '~/apis/uc/user' // 改用已有的查询用户接口

const props = defineProps<{
  roleKey: string
  roleName: string
}>()

const dialogVisible = ref(false)
const loading = ref(false)
const tableData = ref([])

// 搜索数据
const searchData = reactive({
  userName: '',
  roleKey: ''
})

// 分页数据
const paginationData = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 新增用户授权相关
const addUserDialogVisible = ref(false)
const userSearchLoading = ref(false)
const userOptions = ref([])
const addUserForm = reactive({
  userId: undefined as number | undefined,
  roleScope: '',
  expireTime: ''
})

// 日期快捷选项
const dateShortcuts = [
  {
    text: '一周后',
    value: () => {
      const date = new Date()
      date.setTime(date.getTime() + 3600 * 1000 * 24 * 7)
      return date
    }
  },
  {
    text: '一个月后',
    value: () => {
      const date = new Date()
      date.setMonth(date.getMonth() + 1)
      return date
    }
  },
  {
    text: '三个月后',
    value: () => {
      const date = new Date()
      date.setMonth(date.getMonth() + 3)
      return date
    }
  },
  {
    text: '一年后',
    value: () => {
      const date = new Date()
      date.setMonth(date.getMonth() + 12)
      return date
    }
  },
  {
    text: '两年后',
    value: () => {
      const date = new Date()
      date.setMonth(date.getMonth() + 24)
      return date
    }
  }
]

// 获取表格数据
const getTableData = async () => {
  loading.value = true
  try {
    const res = await queryUserRoles({
      pageNo: paginationData.currentPage,
      pageSize: paginationData.pageSize,
      roleKey: props.roleKey,
      userName: searchData.userName
    })
    const data = res.data
    paginationData.total = data.total
    tableData.value = data.records
  } catch (err: any) {
    ElMessage.error(err.message || '获取用户列表失败')
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

// 显示新增用户对话框
const showAddUserDialog = () => {
  addUserForm.userId = undefined
  addUserForm.roleScope = ''
  addUserForm.expireTime = ''
  userOptions.value = []
  addUserDialogVisible.value = true
}

// 搜索用户
const handleUserSearch = async (query: string) => {
  if (query) {
    userSearchLoading.value = true
    try {
      const res = await queryUsersByAdmin({
        userName: query,
        pageNo: 1,
        pageSize: 20
      })
      userOptions.value = res.data.records || []
    } catch (err: any) {
      ElMessage.error(err.message || '搜索用户失败')
    } finally {
      userSearchLoading.value = false
    }
  } else {
    userOptions.value = []
  }
}

// 新增用户授权
const handleAddUser = async () => {
  if (!addUserForm.userId) {
    ElMessage.warning('请选择用户')
    return
  }
  try {
    await assignUserRole({
      userId: addUserForm.userId,
      roleKey: props.roleKey,
      roleScope: addUserForm.roleScope || undefined,
      expireTime: addUserForm.expireTime || undefined
    })
    ElMessage.success('授权成功')
    addUserDialogVisible.value = false
    getTableData()
  } catch (err: any) {
    ElMessage.error(err.message || '授权失败')
  }
}

// 取消用户角色
const cancelUserRole = (row: any) => {
  ElMessageBox.confirm('确认取消该用户的角色授权吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await cancelUserRoleApi(row.id)
      ElMessage.success('取消授权成功')
      getTableData()
    } catch (err: any) {
      ElMessage.error(err.message || '取消授权失败')
    }
  })
}

// 监听分页参数变化
watch([() => paginationData.currentPage, () => paginationData.pageSize], () => {
  getTableData()
})

// 监听 roleKey 的变化
watch(
  () => props.roleKey,
  (newKey) => {
    if (dialogVisible.value && newKey) {
      getTableData()
    }
  }
)

// 对外暴露方法
const showDialog = () => {
  dialogVisible.value = true
  searchData.userName = ''
  searchData.roleKey = props.roleKey
}

defineExpose({
  showDialog
})
</script>

<style lang="scss" scoped>
.search-wrapper {
  margin-bottom: 20px;
}
.pager-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
