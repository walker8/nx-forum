<template>
  <div class="nx-container">
    <el-card shadow="never">
      <el-tabs v-model="activeTab">
        <!-- 基本信息标签页 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
            <el-form-item label="版块号" prop="name" required>
              <el-input v-model="formData.name" placeholder="请输入版块号(2-20位字母数字)" />
            </el-form-item>
            <el-form-item label="版块名称" prop="nickName" required>
              <el-input v-model="formData.nickName" placeholder="请输入版块名称(2-20字符)" />
            </el-form-item>
            <el-form-item label="版块简介" prop="shortBrief">
              <el-input
                v-model="formData.shortBrief"
                placeholder="请输入版块简介(30字以内)"
                maxlength="30"
                show-word-limit
              />
            </el-form-item>
            <el-form-item label="详细介绍" prop="brief">
              <el-input
                v-model="formData.brief"
                type="textarea"
                :rows="4"
                placeholder="请输入详细介绍(支持HTML)"
              />
            </el-form-item>
            <el-form-item label="SEO标题" prop="seoTitle">
              <el-input v-model="formData.seoTitle" placeholder="SEO标题(可选)" />
            </el-form-item>
            <el-form-item label="SEO关键字" prop="seoKeywords">
              <el-input
                v-model="formData.seoKeywords"
                placeholder="SEO关键字(最多6个,用英文逗号分隔)"
              />
            </el-form-item>
            <el-form-item label="SEO描述" prop="seoContent">
              <el-input
                v-model="formData.seoContent"
                type="textarea"
                :rows="2"
                placeholder="SEO描述内容"
              />
            </el-form-item>
            <el-form-item label="SEO后缀" prop="seoExt">
              <el-input v-model="formData.seoExt" placeholder="SEO帖子后缀" />
            </el-form-item>
            <el-form-item label="背景图片" prop="backgroundImage">
              <el-input v-model="formData.backgroundImage" disabled placeholder="背景图片URL" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitBasicForm">保存基本信息</el-button>
              <el-button @click="router.back()">返回</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 权限设置标签页 -->
        <el-tab-pane label="权限设置" name="permissions" v-if="formData.forumId > 10">
          <el-alert style="margin-bottom: 20px" type="warning">
            当前版块：{{
              formData.nickName
            }}，开启权限后，当前版块的用户组权限将覆盖论坛通用的用户权限</el-alert
          >
          <el-form :model="permissionForm" label-width="100px">
            <el-form-item label="启用权限">
              <el-switch v-model="permissionForm.enableForumAccess" />
            </el-form-item>

            <el-table
              :data="permissionForm.permissions"
              border
              table-layout="fixed"
              class="permission-table"
            >
              <el-table-column label="用户组" prop="roleName" width="180" />
              <el-table-column label="访问版块" align="center" width="120">
                <template #default="{ row }">
                  <el-checkbox
                    :checked="row.perms?.includes('forum:visit:section')"
                    @change="(checked) => handlePermChange(checked, row, 'forum:visit:section')"
                  />
                </template>
              </el-table-column>
              <el-table-column label="查看主题" align="center" width="120">
                <template #default="{ row }">
                  <el-checkbox
                    :checked="row.perms?.includes('thread:view')"
                    @change="(checked) => handlePermChange(checked, row, 'thread:view')"
                  />
                </template>
              </el-table-column>
              <el-table-column label="发表主题" align="center" width="120">
                <template #default="{ row }">
                  <el-checkbox
                    :checked="row.perms?.includes('thread:new')"
                    @change="(checked) => handlePermChange(checked, row, 'thread:new')"
                  />
                </template>
              </el-table-column>
              <el-table-column label="发表评论" align="center" width="120">
                <template #default="{ row }">
                  <el-checkbox
                    :checked="row.perms?.includes('comment:new')"
                    @change="(checked) => handlePermChange(checked, row, 'comment:new')"
                  />
                </template>
              </el-table-column>
            </el-table>

            <div style="margin-top: 20px">
              <el-button type="primary" @click="submitPermissionForm">保存权限设置</el-button>
              <el-button @click="router.back()">返回</el-button>
            </div>
          </el-form>
        </el-tab-pane>

        <!-- 用户组标签页 -->
        <el-tab-pane label="用户组" name="userRoles" v-if="formData.forumId > 10">
          <el-alert style="margin-bottom: 20px" type="warning">
            当前版块：{{ formData.nickName }}，仅显示当前版块的用户组
          </el-alert>

          <el-form :inline="true" class="search-form">
            <el-form-item label="用户名">
              <el-input v-model="userRoleQuery.userName" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="用户组">
              <el-select
                v-model="userRoleQuery.roleKey"
                class="min-w-[180px]"
                placeholder="请选择用户组"
                clearable
              >
                <el-option label="版主" value="MODERATOR" />
                <el-option label="内容创作者" value="CREATOR" />
                <el-option label="禁言用户" value="MUTED_USER" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadUserRoles">查询</el-button>
              <el-button @click="resetUserRoleQuery">重置</el-button>
              <el-button type="success" @click="openAssignDialog">授权</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="userRoleList" border v-loading="userRoleLoading">
            <el-table-column label="用户名" prop="userName" />
            <el-table-column label="用户组" prop="roleName" />
            <el-table-column label="授权人" prop="createUserName" />
            <el-table-column label="授权时间" prop="createTime" />
            <el-table-column label="过期时间" prop="expireTime" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button
                  type="warning"
                  link
                  @click="handleUnban(row)"
                  v-if="row.roleKey === 'MUTED_USER'"
                >
                  解除禁言
                </el-button>
                <el-button type="danger" link @click="handleCancelAuthorization(row)" v-else>
                  取消授权
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-container">
            <el-pagination
              v-model:current-page="userRoleQuery.pageNo"
              v-model:page-size="userRoleQuery.pageSize"
              :total="userRoleTotal"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @size-change="loadUserRoles"
              @current-change="loadUserRoles"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 授权对话框 -->
    <el-dialog v-model="assignDialogVisible" title="用户组授权" width="500px">
      <el-form :model="assignForm" label-width="100px">
        <el-form-item label="用户名" required>
          <el-autocomplete
            v-model="assignForm.userName"
            :fetch-suggestions="queryUserSearch"
            placeholder="请输入用户名"
            @select="handleUserSelect"
            :trigger-on-focus="false"
            value-key="userName"
          />
        </el-form-item>
        <el-form-item label="用户组" required>
          <el-select v-model="assignForm.roleKey" placeholder="请选择用户组">
            <el-option label="版主" value="MODERATOR" />
            <el-option label="内容创作者" value="CREATOR" />
          </el-select>
        </el-form-item>
        <el-form-item label="过期时间">
          <el-date-picker
            v-model="assignForm.expireTime"
            type="datetime"
            style="width: 100%"
            placeholder="请选择过期时间，留空为永久"
            :shortcuts="expireTimeShortcuts"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignAuthorization">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import {
  getForumById,
  updateForum,
  getForumAccess,
  batchUpdateForumAccess,
  queryForumUserRoles,
  assignForumUserRole,
  cancelForumUserRole
} from '~/apis/forum'
import { queryUsername } from '~/apis/uc/user'
import type { AutocompleteData } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { unbanUser } from '~/apis/ban'

definePageMeta({
  layout: 'admin'
})

const router = useRouter()
const route = useRoute()
const forumId = Number(route.params.id)
const activeTab = ref('basic')

// 基本信息表单
const formRef = ref()
const formData = reactive({
  forumId: forumId,
  name: '',
  nickName: '',
  iconName: '',
  shortBrief: '',
  brief: '',
  forumAccess: 0,
  seoTitle: '',
  seoKeywords: '',
  seoContent: '',
  seoExt: '',
  backgroundImage: '',
  showMenu: true,
  menuOrder: 0
})

// 权限设置表单
const permissionForm = reactive({
  enableForumAccess: false,
  permissions: [] as any[]
})

// 用户组相关数据
const userRoleQuery = reactive({
  forumId: forumId,
  pageNo: 1,
  pageSize: 10,
  userName: '',
  roleKey: ''
})

const userRoleList = ref([])
const userRoleTotal = ref(0)
const userRoleLoading = ref(false)

// 授权表单
const assignDialogVisible = ref(false)
const assignForm = reactive({
  userId: null,
  userName: '',
  roleKey: '',
  expireTime: undefined as undefined | string
})

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
  ],
  shortBrief: [{ max: 30, message: '简介不能超过20字', trigger: 'blur' }],
  seoKeywords: [
    {
      pattern: /^[^,]+(,[^,]+){0,5}$/,
      message: '最多输入6个关键字，用英文逗号分隔',
      trigger: 'blur'
    }
  ]
}

// 获取版块基本信息
const getForumData = async () => {
  try {
    const res = await getForumById(forumId)
    if (res.data) {
      const {
        name,
        nickName,
        iconName,
        showMenu,
        menuOrder,
        shortBrief,
        brief,
        forumAccess,
        seoTitle,
        seoKeywords,
        seoContent,
        seoExt,
        backgroundImage
      } = res.data
      Object.assign(formData, {
        name,
        nickName,
        iconName,
        showMenu,
        menuOrder,
        shortBrief,
        brief,
        forumAccess,
        seoTitle,
        seoKeywords,
        seoContent,
        seoExt,
        backgroundImage
      })
      permissionForm.enableForumAccess = !!forumAccess
    }
  } catch (error) {
    ElMessage.error(error || '获取版块信息失败')
    router.back()
  }
}

// 获取版块权限信息
const getPermissionData = async () => {
  try {
    const res = await getForumAccess(forumId)
    if (res.data) {
      permissionForm.permissions = res.data
    }
  } catch (error) {
    ElMessage.error(error || '获取版块权限信息失败')
  }
}

// 提交基本信息表单
const submitBasicForm = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    await updateForum({ ...formData })
    ElMessage.success('更新版块基本信息成功')
  } catch (error) {
    ElMessage.error(error || '更新版块基本信息失败')
  }
}

// 提交权限设置表单
const submitPermissionForm = async () => {
  try {
    // 移除多余的权限标识
    permissionForm.permissions.forEach((item: any) => {
      item.perms = item.perms?.filter((perm: string) =>
        ['thread:new', 'thread:view', 'forum:visit:section', 'comment:new'].includes(perm)
      )
    })
    await batchUpdateForumAccess(
      forumId,
      permissionForm.permissions,
      permissionForm.enableForumAccess
    )
    ElMessage.success('更新版块权限成功')
  } catch (error) {
    ElMessage.error(error || '更新版块权限失败')
  }
}

// 添加权限变更处理函数
const handlePermChange = (checked: string | number | boolean, row: any, perm: string) => {
  if (!Array.isArray(row.perms)) {
    row.perms = []
  }

  if (!!checked) {
    // 使用!!转换为布尔值
    if (!row.perms.includes(perm)) {
      row.perms.push(perm)
    }
  } else {
    const index = row.perms.indexOf(perm)
    if (index > -1) {
      row.perms.splice(index, 1)
    }
  }
}

// 加载用户组列表
const loadUserRoles = async () => {
  try {
    userRoleLoading.value = true
    const res = await queryForumUserRoles(userRoleQuery)
    if (res.data) {
      userRoleList.value = res.data.records
      userRoleTotal.value = res.data.total
    }
  } catch (error) {
    ElMessage.error(error || '获取用户组列表失败')
  } finally {
    userRoleLoading.value = false
  }
}

// 重置查询条件
const resetUserRoleQuery = () => {
  userRoleQuery.userName = ''
  userRoleQuery.roleKey = ''
  userRoleQuery.pageNo = 1
  loadUserRoles()
}

// 用户搜索建议
const queryUserSearch = (
  queryString: string,
  cb: (suggestions: AutocompleteData[]) => void
): void => {
  if (queryString.length < 1) {
    cb([])
    return
  }

  queryUsername(queryString)
    .then((res) => {
      if (res.data) {
        const suggestions: AutocompleteData[] = res.data.map((item: any) => ({
          userName: item.userName,
          userId: item.userId
        }))
        cb(suggestions)
      } else {
        cb([])
      }
    })
    .catch((error) => {
      console.error('查询用户失败:', error)
      cb([])
    })
}

// 处理用户选择
const handleUserSelect = (item: any) => {
  assignForm.userName = item.userName
  assignForm.userId = item.userId
}

// 打开授权对话框
const openAssignDialog = () => {
  assignForm.userId = null
  assignForm.userName = ''
  assignForm.roleKey = ''
  assignForm.expireTime = undefined
  assignDialogVisible.value = true
}

// 格式化日期为 yyyy-MM-dd HH:mm:ss
const formatDateTime = (date: Date) => {
  const pad = (n: number) => n.toString().padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

// 处理授权
const handleAssignAuthorization = async () => {
  if (!assignForm.userId) {
    ElMessage.warning('请选择用户')
    return
  }

  if (!assignForm.roleKey) {
    ElMessage.warning('请选择角色标识')
    return
  }

  try {
    // 格式化过期时间
    const expireTime = assignForm.expireTime
      ? formatDateTime(new Date(assignForm.expireTime))
      : undefined

    await assignForumUserRole({
      forumId,
      userId: assignForm.userId,
      roleKey: assignForm.roleKey,
      expireTime
    })
    ElMessage.success('授权成功')
    assignDialogVisible.value = false
    loadUserRoles()
  } catch (error) {
    ElMessage.error(error || '授权失败')
  }
}

// 处理取消授权
const handleCancelAuthorization = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要取消该用户的角色授权吗？', '提示', {
      type: 'warning'
    })

    await cancelForumUserRole(forumId, row.roleKey)
    ElMessage.success('取消授权成功')
    loadUserRoles()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error || '取消授权失败')
    }
  }
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
        forumId,
        reason: ''
      })
        .then(() => {
          ElMessage.success('解除禁言成功')
          loadUserRoles()
        })
        .catch((err) => ElMessage.error(err))
    })
    .catch(() => {})
}

// 监听标签页切换
watch(activeTab, (tab) => {
  if (tab === 'userRoles') {
    loadUserRoles()
  }
})

// 页面加载时获取数据
onMounted(() => {
  getForumData()
  getPermissionData()
})

// 可以添加角色常量定义
const FORUM_ROLES = [
  { label: '版主', value: 'MODERATOR' },
  { label: '内容创作者', value: 'CREATOR' },
  { label: '禁言用户', value: 'MUTED_USER' }
] as const

// 过期时间快捷选项
const expireTimeShortcuts = [
  {
    text: '一个月',
    value: () => {
      const date = new Date()
      date.setMonth(date.getMonth() + 1)
      return date
    }
  },
  {
    text: '两个月',
    value: () => {
      const date = new Date()
      date.setMonth(date.getMonth() + 2)
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
      date.setMonth(date.getMonth() + 12)
      return date
    }
  },
  {
    text: '两年',
    value: () => {
      const date = new Date()
      date.setMonth(date.getMonth() + 24)
      return date
    }
  }
]
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.permission-table {
  width: 100%;
  margin-bottom: 20px;
}

:deep(.el-table__cell) {
  padding: 12px;
}

.search-form {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>
