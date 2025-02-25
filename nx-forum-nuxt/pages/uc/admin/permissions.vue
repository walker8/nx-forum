<template>
  <div class="nx-container">
    <el-card shadow="never">
      <div class="toolbar-wrapper">
        <div>
          <el-button type="success" :icon="Plus" @click="addPermission">新增权限</el-button>
        </div>
        <div>
          <el-tooltip content="刷新">
            <el-button type="primary" :icon="RefreshRight" circle @click="getTableData" />
          </el-tooltip>
        </div>
      </div>

      <div class="table-wrapper">
        <el-table :data="tableData" v-loading="loading" border row-key="permId">
          <el-table-column prop="permName" label="权限名称" min-width="160" />
          <el-table-column prop="permOrder" label="排序id" width="80" align="center" />
          <el-table-column prop="perms" label="权限编码" min-width="180" align="center" />
          <el-table-column prop="permType" label="权限类型" width="100" align="center">
            <template #default="scope">
              <el-tag :type="getPermTypeTag(scope.row.permType)">
                {{ getPermTypeLabel(scope.row.permType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="permStatus" label="状态" width="100" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.permStatus === 0 ? 'success' : 'danger'">
                {{ scope.row.permStatus === 0 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center" :fixed="'right'">
            <template #default="scope">
              <el-button-group>
                <el-button type="primary" text @click="editPermission(scope.row)">编辑</el-button>
                <el-button type="primary" text @click="copyPermission(scope.row)">复制</el-button>
                <el-button type="primary" text @click="toggleStatus(scope.row)">
                  {{ scope.row.permStatus === 0 ? '禁用' : '启用' }}
                </el-button>
                <el-button type="danger" text @click="deletePermission(scope.row.permId)">
                  删除
                </el-button>
              </el-button-group>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 权限表单对话框 -->
    <el-dialog v-model="dialogVisible" :title="form.permId ? '编辑权限' : '新增权限'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="权限名称" prop="permName">
          <el-input v-model="form.permName" />
        </el-form-item>
        <el-form-item label="权限编码" prop="perms" v-if="form.permType !== 1">
          <el-input v-model="form.perms" placeholder="请输入权限编码" />
        </el-form-item>
        <el-form-item label="权限类型" prop="permType">
          <el-select v-model="form.permType" placeholder="请选择权限类型" style="width: 100%">
            <el-option label="目录" :value="1" />
            <el-option label="菜单" :value="2" />
            <el-option label="按钮" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="父级权限" prop="parentId">
          <el-tree-select
            :data="permissionList"
            :props="{ label: 'permName', value: 'permId' }"
            placeholder="请选择父级权限"
            style="width: 100%"
            :check-strictly="true"
            clearable
            :model-value="form.parentId === 0 ? null : form.parentId"
            @update:model-value="(val) => (form.parentId = val || 0)"
          />
        </el-form-item>
        <el-form-item label="排序" prop="permOrder">
          <el-input-number v-model="form.permOrder" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { Plus, RefreshRight } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getPermissionList,
  createPermission,
  updatePermission,
  deletePermission as deletePermissionApi,
  enablePermission,
  disablePermission
} from '~/apis/uc/permission'
import type { PermissionCreateCmd, PermissionUpdateCmd } from '~/types/global'

definePageMeta({
  layout: 'uc'
})

// 表格数据
const loading = ref(false)
const tableData = ref<any[]>([])
const permissionList = ref<any[]>([])

// 表单数据
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({
  permId: '',
  permName: '',
  perms: '',
  permType: 1,
  parentId: null,
  permOrder: 0,
  remark: '',
  permStatus: 1
})

// 表单验证规则
const rules: FormRules = {
  permName: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
  perms: [
    {
      required: true,
      message: '请输入权限编码',
      trigger: 'blur',
      validator: (rule, value, callback) => {
        if (form.permType === 1) {
          callback()
        } else if (!value) {
          callback(new Error('请输入权限编码'))
        } else {
          callback()
        }
      }
    }
  ],
  permType: [{ required: true, message: '请选择权限类型', trigger: 'change' }]
}

// 获取表格数据
const getTableData = async () => {
  loading.value = true
  try {
    const res = await getPermissionList()
    tableData.value = res.data
    permissionList.value = res.data
  } catch (err: any) {
    ElMessage.error(err || '获取权限列表失败')
  } finally {
    loading.value = false
  }
}

// 获取权限类型标签类型
const getPermTypeTag = (type: number) => {
  switch (type) {
    case 1:
      return 'warning'
    case 2:
      return 'success'
    case 3:
      return 'info'
    default:
      return 'danger'
  }
}

// 获取权限类型标签文本
const getPermTypeLabel = (type: number) => {
  switch (type) {
    case 1:
      return '目录'
    case 2:
      return '菜单'
    case 3:
      return '按钮'
    default:
      return '未知'
  }
}

// 新增权限
const addPermission = () => {
  form.permId = ''
  form.permName = ''
  form.perms = ''
  form.permType = 1
  form.parentId = null
  form.permOrder = 0
  form.remark = ''
  form.permStatus = 1
  dialogVisible.value = true
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 编辑权限
const editPermission = (row: any) => {
  Object.assign(form, row)
  dialogVisible.value = true
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 复制权限
const copyPermission = (row: any) => {
  Object.assign(form, {
    ...row,
    permId: '', // 清空ID表示新增
    perms: row.permType !== 1 ? `${row.perms}:copy` : '', // 非目录类型添加copy后缀
    parentId: row.parentId
  })
  dialogVisible.value = true
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 监听对话框关闭
watch(dialogVisible, (val) => {
  if (!val) {
    formRef.value?.clearValidate()
  }
})

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid, fields) => {
    if (valid) {
      try {
        if (form.permId) {
          const updateCmd: PermissionUpdateCmd = {
            parentId: Number(form.parentId),
            permId: Number(form.permId),
            permName: form.permName,
            perms: form.permType === 1 ? '' : form.perms,
            permType: form.permType,
            permStatus: form.permStatus,
            remark: form.remark,
            permOrder: form.permOrder
          }
          await updatePermission(updateCmd)
          ElMessage.success('更新权限成功')
        } else {
          const createCmd: PermissionCreateCmd = {
            permName: form.permName,
            permType: form.permType,
            perms: form.permType === 1 ? '' : form.perms,
            remark: form.remark,
            permOrder: form.permOrder,
            parentId: Number(form.parentId)
          }
          await createPermission(createCmd)
          ElMessage.success('创建权限成功')
        }
        dialogVisible.value = false
        getTableData()
      } catch (err: any) {
        ElMessage.error(err || '操作失败')
      }
    }
  })
}

// 删除权限
const deletePermission = (permId: number) => {
  ElMessageBox.confirm('确认删除该权限吗？删除后无法恢复！', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deletePermissionApi(permId)
      ElMessage.success('删除权限成功')
      getTableData()
    } catch (err: any) {
      ElMessage.error(err || '删除失败')
    }
  })
}

// 切换状态
const toggleStatus = (row: any) => {
  const action = row.permStatus === 0 ? '禁用' : '启用'
  ElMessageBox.confirm(`确认${action}该权限吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      if (row.permStatus === 0) {
        await disablePermission(row.permId)
      } else {
        await enablePermission(row.permId)
      }
      ElMessage.success(`${action}成功`)
      getTableData()
    } catch (err: any) {
      ElMessage.error(err || `${action}失败`)
    }
  })
}

// 初始化
onMounted(() => {
  getTableData()
})
</script>

<style lang="scss" scoped>
.toolbar-wrapper {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
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
