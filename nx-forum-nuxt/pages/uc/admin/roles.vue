<template>
  <div class="nx-container">
    <el-card shadow="never">
      <div class="search-wrapper">
        <el-form ref="searchFormRef" :inline="true" :model="searchData" label-width="auto">
          <el-form-item prop="roleName" label="角色名称">
            <el-input v-model.trim="searchData.roleName" placeholder="请输入角色名称" />
          </el-form-item>
          <el-form-item prop="roleStatus" label="状态">
            <el-select
              v-model="searchData.roleStatus"
              placeholder="请选择状态"
              clearable
              style="width: 160px"
            >
              <el-option label="启用" :value="0" />
              <el-option label="禁用" :value="1" />
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
          <el-button type="success" :icon="Plus" @click="addRole">新增角色</el-button>
        </div>
        <div>
          <el-tooltip content="刷新">
            <el-button type="primary" :icon="RefreshRight" circle @click="getTableData" />
          </el-tooltip>
        </div>
      </div>

      <div class="table-wrapper">
        <el-table :data="tableData" v-loading="loading" border>
          <el-table-column prop="roleId" label="角色ID" width="80" align="center" />
          <el-table-column prop="roleName" label="角色名称" min-width="120" align="center" />
          <el-table-column prop="roleKey" label="角色编码" min-width="120" align="center" />
          <el-table-column prop="remark" label="描述" min-width="180" align="center" />
          <el-table-column prop="priority" label="优先级" width="100" align="center" />
          <el-table-column label="操作" width="200" align="center" fixed="right">
            <template #default="scope">
              <el-button-group>
                <el-button type="primary" text @click="editRole(scope.row)">编辑</el-button>
                <el-button type="primary" text @click="assignUserRole(scope.row)"> 授权 </el-button>
                <el-button
                  type="danger"
                  text
                  @click="deleteRole(scope.row.roleId)"
                  :disabled="scope.row.roleId === 1"
                >
                  删除
                </el-button>
              </el-button-group>
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

    <!-- 角色表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.roleId ? '编辑角色' : '新增角色'"
      width="500px"
      :center="true"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleKey">
          <el-input v-model="form.roleKey" :disabled="!!form.roleId" />
        </el-form-item>
        <el-form-item label="描述" prop="remark">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input-number
            v-model="form.priority"
            :min="0"
            :max="999"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="资源权限">
          <div class="menu-tree-container">
            <el-tree
              :key="treeKey"
              ref="menuTreeRef"
              :data="permissionList"
              show-checkbox
              node-key="permId"
              :props="{
                label: (data: any) => {
                  return data.perms ? `${data.permName} (${data.perms})` : data.permName
                },
                children: 'children',
                disabled: (data: any) => data.permStatus === 1
              }"
              :default-checked-keys="form.permIds"
              default-expand-all
              @check="handleMenuCheck"
            >
              <template #default="{ node, data }">
                <span class="custom-tree-node">
                  <span>{{ data.permName }}</span>
                  <span v-if="data.perms" class="permission-code">{{ data.perms }}</span>
                </span>
              </template>
            </el-tree>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
  <uc-role-users-dialog
    ref="roleUsersDialogRef"
    :role-key="currentRole.roleKey"
    :role-name="currentRole.roleName"
  />
</template>

<script setup lang="ts">
import { Plus, RefreshRight, Search, Refresh } from '@element-plus/icons-vue'
import type { FormInstance, FormRules, ElTree } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  queryRoles,
  createRole,
  updateRole,
  deleteRole as deleteRoleApi,
  getRoleInfo
} from '~/apis/uc/role'
import { getPermissionList } from '~/apis/uc/permission'
import type { RoleUpdateCmd } from '~/types/global'

definePageMeta({
  layout: 'uc'
})

// 表格数据
const loading = ref(false)
const tableData = ref([])

// 表单数据
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const treeKey = ref(0)
const form = reactive({
  roleId: '',
  roleName: '',
  roleKey: '',
  remark: '',
  priority: 0,
  permIds: [] as number[]
})

// 表单验证规则
const rules: FormRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

// 搜索数据
const searchFormRef = ref()
const searchData = reactive({
  roleName: '',
  roleStatus: undefined
})

// 分页数据
const paginationData = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 权限树相关
const menuTreeRef = ref<InstanceType<typeof ElTree>>()
const permissionList = ref<any[]>([])

// 在组件中增加权限映射
const permissionMap = computed(() => {
  const map = new Map<number, string>()
  const traverse = (nodes: any[]) => {
    nodes.forEach((node) => {
      if (node.permId && node.perms) {
        map.set(node.permId, node.perms)
      }
      if (node.children) {
        traverse(node.children)
      }
    })
  }
  traverse(permissionList.value)
  return map
})

// 修改转换函数，过滤父节点权限
const convertPermsToIds = (perms: string[]) => {
  const ids: number[] = []
  const traverse = (nodes: any[]) => {
    nodes.forEach((node) => {
      // 只处理叶子节点或明确包含权限标识的节点
      if (node.perms && perms.includes(node.perms)) {
        // 排除有子节点的父级权限（半选状态）
        if (!node.children || node.children.length === 0) {
          ids.push(node.permId)
        }
      }
      if (node.children) {
        traverse(node.children)
      }
    })
  }
  traverse(permissionList.value)
  return ids
}

// 获取权限列表
const getPermissions = async () => {
  try {
    const res = await getPermissionList()
    permissionList.value = res.data
  } catch (err: any) {
    ElMessage.error(err.message || '获取权限列表失败')
  }
}

let permIds = [] as number[]
// 处理权限选择
const handleMenuCheck = (node: any, checkedInfo: any) => {
  permIds = [...checkedInfo.checkedKeys, ...checkedInfo.halfCheckedKeys]
}

// 获取表格数据
const getTableData = async () => {
  loading.value = true
  try {
    const res = await queryRoles({
      pageNo: paginationData.currentPage,
      pageSize: paginationData.pageSize,
      roleName: searchData.roleName || undefined,
      roleStatus: searchData.roleStatus
    })
    const data = res.data
    paginationData.total = data.total
    tableData.value = data.records
  } catch (err: any) {
    ElMessage.error(err.message || '获取角色列表失败')
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

// 重置搜索
const resetSearch = () => {
  searchFormRef.value?.resetFields()
  handleSearch()
}

/** 监听分页参数的变化 */
watch([() => paginationData.currentPage, () => paginationData.pageSize], getTableData, {
  immediate: true
})

// 新增角色
const addRole = () => {
  form.roleId = ''
  form.roleName = ''
  form.roleKey = ''
  form.remark = ''
  form.priority = 0
  form.permIds = []
  dialogVisible.value = true
  treeKey.value++
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 编辑角色
const editRole = (row: any) => {
  getRoleInfo(row.roleId)
    .then((res: any) => {
      Object.assign(form, res.data)
      // 添加转换逻辑
      permIds = form.permIds = convertPermsToIds(res.data.perms || [])
      dialogVisible.value = true
      treeKey.value++
      nextTick(() => {
        formRef.value?.clearValidate()
      })
    })
    .catch((err: any) => {
      ElMessage.error(err.message || '获取角色详情失败')
    })
}

// 修改提交逻辑
const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid, fields) => {
    if (valid) {
      try {
        // 转换权限ID为权限标识符
        const perms = permIds.map((id) => permissionMap.value.get(id)).filter(Boolean) as string[]

        const payload = {
          ...form,
          perms, // 添加转换后的perms字段
          permIds: undefined // 移除原始permIds字段
        }

        if (form.roleId) {
          const updateCmd: RoleUpdateCmd = {
            ...payload,
            roleId: Number(form.roleId),
            roleStatus: 0 // 默认启用状态
          }
          await updateRole(updateCmd)
          ElMessage.success('更新角色成功')
        } else {
          await createRole(payload)
          ElMessage.success('创建角色成功')
        }
        dialogVisible.value = false
        getTableData()
      } catch (err: any) {
        ElMessage.error(err.message || '操作失败')
      }
    }
  })
}

// 删除角色
const deleteRole = (roleId: number) => {
  ElMessageBox.confirm('确认删除该角色吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteRoleApi(roleId)
      ElMessage.success('删除角色成功')
      getTableData()
    } catch (err: any) {
      ElMessage.error(err.message || '删除失败')
    }
  })
}

// 角色用户授权相关
const roleUsersDialogRef = ref()
const currentRole = ref({
  roleKey: '',
  roleName: ''
})
// 分配权限方法
const assignUserRole = (row: any) => {
  currentRole.value = {
    roleKey: row.roleKey,
    roleName: row.roleName
  }
  roleUsersDialogRef.value?.showDialog()
}

// 初始化
onMounted(() => {
  getTableData()
  getPermissions()
})

// 监听对话框关闭
watch(dialogVisible, (val) => {
  if (!val) {
    formRef.value?.clearValidate()
  }
})
</script>

<style lang="scss" scoped>
.search-wrapper {
  margin-bottom: 20px;
}
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
:deep(.el-button-group) {
  .el-button {
    padding-left: 5px;
    padding-right: 5px;
    & + .el-button {
      margin-left: -1px;
    }
  }
}
.menu-tree-container {
  border: 1px solid var(--el-border-color-light);
  border-radius: 4px;
  padding: 10px;
  width: 100%;
  max-height: 400px;
  overflow-y: auto;
  :deep(.el-tree) {
    height: 100%;
    .el-tree-node {
      white-space: nowrap;
    }
  }

  :deep(.custom-tree-node) {
    display: flex;
    align-items: center;
    gap: 8px;

    .permission-code {
      color: var(--el-text-color-secondary);
      font-size: 13px;
    }
  }
}
:deep(.el-dialog) {
  margin-top: 0 !important;
  top: 50%;
  transform: translateY(-50%);
}
</style>
