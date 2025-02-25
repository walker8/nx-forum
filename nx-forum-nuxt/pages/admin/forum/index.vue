<template>
  <div class="nx-container">
    <el-card shadow="never">
      <div class="toolbar-wrapper">
        <div>
          <el-button type="success" :icon="Plus" size="default" @click="showAddDialog">
            增加版块
          </el-button>
        </div>
        <div>
          <el-tooltip content="刷新当前页">
            <el-button
              type="primary"
              :icon="RefreshRight"
              circle
              size="small"
              @click="getTableData"
            />
          </el-tooltip>
        </div>
      </div>
      <div class="mb-3">
        <el-alert>
          Tabler图标地址
          <a
            href="https://icon-sets.iconify.design/tabler/"
            target="_blank"
            class="hover:text-blue-400"
          >
            https://icon-sets.iconify.design/tabler/
          </a>
        </el-alert>
      </div>
      <div class="table-wrapper">
        <el-table :data="tableData" border>
          <el-table-column prop="forumId" label="版块ID" width="100" align="center" />
          <el-table-column prop="name" label="版块号" min-width="110" align="center" />
          <el-table-column prop="nickName" label="版块名称" min-width="100" align="center" />
          <el-table-column prop="iconName" label="版块图标" width="320" align="left">
            <template #default="scope">
              <el-input
                v-model="scope.row.iconName"
                style="width: 240px"
                placeholder="请输入版块图标"
                clearable
                @change="update(scope.row)"
              />
              <el-icon size="20" style="margin-left: 10px">
                <Icon :name="scope.row.iconName" />
              </el-icon>
            </template>
          </el-table-column>
          <el-table-column prop="showMenu" label="菜单显示" min-width="100" align="left">
            <template #default="scope">
              <el-switch v-model="scope.row.showMenu" @change="update(scope.row)" />
            </template>
          </el-table-column>
          <el-table-column prop="menuOrder" label="菜单排序" min-width="130" align="center">
            <template #default="scope">
              <el-input
                v-model="scope.row.menuOrder"
                style="width: 100px"
                placeholder="请输入菜单顺序"
                type="number"
                @change="update(scope.row)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="defaultForum" label="默认版块" min-width="100" align="left">
            <template #default="scope">
              <el-switch
                v-model="scope.row.defaultForum"
                :disabled="scope.row.defaultForum"
                @change="updateDefaultForum(scope.row.forumId)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="isSystem" label="分类" min-width="100" align="center">
            <template #default="scope">
              <el-text type="warning" v-if="scope.row.isSystem">系统内置</el-text>
              <el-text type="info" v-else>用户自建</el-text>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="150" align="center" />
          <el-table-column fixed="right" label="操作" width="150" align="center">
            <template #default="scope">
              <el-button-group>
                <el-button
                  type="success"
                  text
                  bg
                  size="small"
                  @click="router.push(`/admin/forum/${scope.row.forumId}/edit`)"
                >
                  编辑
                </el-button>
                <el-button
                  type="danger"
                  text
                  bg
                  size="small"
                  v-if="!scope.row.isSystem"
                  @click="deleteByAdmin(scope.row.forumId)"
                >
                  删除
                </el-button>
              </el-button-group>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
    <el-dialog v-model="dialogVisible" title="新增版块" width="30%">
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="版块号" prop="name" required>
          <el-input v-model="formData.name" placeholder="请输入版块号(2-20位字母数字)" />
        </el-form-item>
        <el-form-item label="版块名称" prop="nickName" required>
          <el-input v-model="formData.nickName" placeholder="请输入版块名称(2-20字符)" />
        </el-form-item>
        <el-form-item label="版块图标" prop="iconName">
          <el-input v-model="formData.iconName" placeholder="请输入图标名称" />
        </el-form-item>
        <el-form-item label="显示菜单">
          <el-switch v-model="formData.showMenu" />
        </el-form-item>
        <el-form-item label="菜单排序">
          <el-input-number v-model="formData.menuOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="submitNewForum">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { Plus, RefreshRight } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  deleteForum,
  getAllForumByAdmin,
  updateDefaultForumByAdmin,
  updateForum,
  type ForumItemVO,
  createForum
} from '~/apis/forum'
import { useRouter } from 'vue-router'

definePageMeta({
  layout: 'admin'
})
const tableData = ref<ForumItemVO[]>([])
const getTableData = () => {
  getAllForumByAdmin()
    .then((res) => {
      tableData.value = res.data
    })
    .catch((errMsg) => {
      ElMessage.error(errMsg)
    })
}
getTableData()

const updateDefaultForum = (forumId: number) => {
  updateDefaultForumByAdmin(forumId).then(() => {
    getTableData()
  })
}
const update = (forumItem: ForumItemVO) => {
  updateForum({ ...forumItem })
    .then(() => {})
    .catch((errMsg) => {
      ElMessage.error(errMsg)
    })
}
const deleteByAdmin = (forumId: number) => {
  ElMessageBox.confirm('确定删除该版块吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      deleteForum(forumId)
        .then(() => {
          ElMessage.success('删除版块成功')
          getTableData()
        })
        .catch((errMsg) => {
          ElMessage.error(errMsg)
        })
    })
    .catch(() => {})
}

const dialogVisible = ref(false)
const formData = reactive({
  name: '',
  nickName: '',
  iconName: '',
  showMenu: true,
  menuOrder: 0
})

const formRef = ref()

const rules = {
  name: [
    { required: true, message: '请输入版块号', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在2到20个字符', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9]+$/,
      message: '版块号只能包含字母和数字',
      trigger: 'blur'
    }
  ],
  nickName: [
    { required: true, message: '请输入版块名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在2到20个字符', trigger: 'blur' }
  ]
}

const resetForm = () => {
  formData.name = ''
  formData.nickName = ''
  formData.iconName = ''
  formData.showMenu = true
  formData.menuOrder = 0
  // 重置表单的校验结果
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

const showAddDialog = () => {
  resetForm()
  dialogVisible.value = true
}

const closeDialog = () => {
  dialogVisible.value = false
  resetForm()
}

const submitNewForum = async () => {
  if (!formRef.value) return

  try {
    // 表单验证
    await formRef.value.validate()

    await createForum({ ...formData })
    ElMessage.success('版块创建成功')
    dialogVisible.value = false
    getTableData()
    resetForm()
  } catch (error) {
    if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error(error || '创建版块失败')
    }
  }
}

const router = useRouter()
</script>

<style lang="scss" scoped>
.toolbar-wrapper {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
}
.pager-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
