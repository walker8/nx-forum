<script setup lang="ts">
import type { InputInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import {
  getAuditConfigSensitiveWords,
  updateAuditConfigSensitiveWords,
  getAuditConfigBlackWhiteUsersVO,
  updateAuditConfigBlackWhiteUsers
} from '~/apis/audit'
import { queryUsername } from '~/apis/uc/user'
import type { UserVO } from '~/types/global'

definePageMeta({
  layout: 'admin'
})

// 当前选中的 tab
const activeTab = ref('sensitive')

// 敏感词配置
const sensitiveConfig = reactive({
  enableSensitiveWordsAudit: false,
  sensitiveWords: [] as string[]
})

// 针对敏感词的添加
const sensitiveInputVisible = ref(false)
const sensitiveInputValue = ref('')
const sensitiveInputRef = ref<InputInstance>()

const handleSensitiveInputConfirm = () => {
  if (
    sensitiveInputValue.value &&
    !sensitiveConfig.sensitiveWords.includes(sensitiveInputValue.value)
  ) {
    sensitiveConfig.sensitiveWords.push(sensitiveInputValue.value)
  }
  sensitiveInputVisible.value = false
  sensitiveInputValue.value = ''
}
const handleSensitiveClose = (word: string) => {
  sensitiveConfig.sensitiveWords = sensitiveConfig.sensitiveWords.filter((w) => w !== word)
}
const showSensitiveInput = () => {
  sensitiveInputVisible.value = true
  nextTick(() => {
    sensitiveInputRef.value?.input?.focus()
  })
}

// 黑白名单配置（使用UserVO结构）
const blackWhiteConfig = reactive({
  whiteListUsers: [] as UserVO[],
  blackListUsers: [] as UserVO[]
})

// 用户搜索相关状态
const whiteSearch = reactive({
  query: '',
  results: [] as UserVO[],
  visible: false
})

const blackSearch = reactive({
  query: '',
  results: [] as UserVO[],
  visible: false
})

// 处理用户搜索
const handleUserSearch = async (type: 'white' | 'black') => {
  const query = type === 'white' ? whiteSearch.query : blackSearch.query
  if (!query.trim()) return

  try {
    const res = await queryUsername(query.trim())
    if (type === 'white') {
      whiteSearch.results = res.data
    } else {
      blackSearch.results = res.data
    }
  } catch (error) {
    ElMessage.error(error || '用户搜索失败')
  }
}

// 添加用户到列表
const addUserToList = (user: UserVO, list: 'white' | 'black', type: 'white' | 'black') => {
  const exists =
    list === 'white'
      ? blackWhiteConfig.whiteListUsers.some((u) => u.userId === user.userId)
      : blackWhiteConfig.blackListUsers.some((u) => u.userId === user.userId)

  if (!exists) {
    if (list === 'white') {
      blackWhiteConfig.whiteListUsers.push(user)
    } else {
      blackWhiteConfig.blackListUsers.push(user)
    }
  }
  if (type === 'white') {
    whiteSearch.visible = false
    whiteSearch.query = ''
  } else {
    blackSearch.visible = false
    blackSearch.query = ''
  }
}

// 获取配置
const getAuditSettings = async () => {
  try {
    const [sensitiveRes, bwRes] = await Promise.all([
      getAuditConfigSensitiveWords(),
      getAuditConfigBlackWhiteUsersVO()
    ])
    Object.assign(sensitiveConfig, sensitiveRes.data)
    Object.assign(blackWhiteConfig, bwRes.data)
  } catch (error) {
    ElMessage.error(error || '获取审核设置失败')
  }
}

// 保存敏感词配置
const saveSensitiveConfig = async () => {
  try {
    await updateAuditConfigSensitiveWords(sensitiveConfig)
    ElMessage.success('敏感词设置保存成功')
  } catch (error) {
    ElMessage.error(error || '敏感词设置保存失败')
  }
}

// 保存黑白名单配置（适配后端接口）
const saveBlackWhiteConfig = async () => {
  try {
    const payload = {
      whiteListUsers: blackWhiteConfig.whiteListUsers.map((u) => u.userId),
      blackListUsers: blackWhiteConfig.blackListUsers.map((u) => u.userId)
    }
    await updateAuditConfigBlackWhiteUsers(payload)
    ElMessage.success('黑白名单设置保存成功')
  } catch (error) {
    ElMessage.error(error || '黑白名单设置保存失败')
  }
}

onMounted(() => {
  getAuditSettings()
})
</script>

<template>
  <el-card shadow="never">
    <el-tabs v-model="activeTab">
      <!-- 敏感词设置 Tab -->
      <el-tab-pane label="敏感词设置" name="sensitive">
        <el-form label-width="auto">
          <el-form-item label="敏感词审核">
            <el-checkbox v-model="sensitiveConfig.enableSensitiveWordsAudit">启用</el-checkbox>
          </el-form-item>
          <el-form-item label="敏感词列表">
            <div class="flex flex-wrap items-center gap-2">
              <el-tag
                v-for="word in sensitiveConfig.sensitiveWords"
                :key="word"
                closable
                @close="handleSensitiveClose(word)"
              >
                {{ word }}
              </el-tag>
              <el-input
                v-if="sensitiveInputVisible"
                ref="sensitiveInputRef"
                v-model="sensitiveInputValue"
                class="input-new-tag"
                size="small"
                @keyup.enter="handleSensitiveInputConfirm"
                @blur="handleSensitiveInputConfirm"
              />
              <el-button
                v-else
                class="button-new-tag"
                size="small"
                type="primary"
                @click="showSensitiveInput"
              >
                + 添加敏感词
              </el-button>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveSensitiveConfig">保存设置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 黑白名单设置 Tab -->
      <el-tab-pane label="黑白名单设置" name="blackwhite">
        <el-form label-width="auto">
          <el-alert type="info" show-icon class="mb-4">
            <p>白名单用户发帖免审核，黑名单用户发帖自动拦截</p>
          </el-alert>

          <!-- 白名单用户展示 -->
          <el-form-item label="白名单用户">
            <div class="user-list flex flex-wrap items-center gap-2">
              <el-tag
                v-for="user in blackWhiteConfig.whiteListUsers"
                :key="user.userId"
                closable
                type="success"
                @close="
                  blackWhiteConfig.whiteListUsers = blackWhiteConfig.whiteListUsers.filter(
                    (u) => u.userId !== user.userId
                  )
                "
              >
                {{ user.userName }} (ID: {{ user.userId }})
              </el-tag>
              <el-button
                class="h-8"
                type="primary"
                size="small"
                @click="whiteSearch.visible = true"
              >
                + 添加用户
              </el-button>
            </div>
          </el-form-item>

          <!-- 黑名单用户展示 -->
          <el-form-item label="黑名单用户">
            <div class="user-list flex flex-wrap items-center gap-2">
              <el-tag
                v-for="user in blackWhiteConfig.blackListUsers"
                :key="user.userId"
                closable
                type="danger"
                @close="
                  blackWhiteConfig.blackListUsers = blackWhiteConfig.blackListUsers.filter(
                    (u) => u.userId !== user.userId
                  )
                "
              >
                {{ user.userName }} (ID: {{ user.userId }})
              </el-tag>
              <el-button class="h-8" type="danger" size="small" @click="blackSearch.visible = true">
                + 添加用户
              </el-button>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveBlackWhiteConfig">保存设置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </el-card>

  <!-- 白名单搜索弹窗 -->
  <el-dialog v-model="whiteSearch.visible" title="添加白名单用户" width="30%">
    <el-input
      v-model="whiteSearch.query"
      placeholder="输入用户名搜索"
      @input="handleUserSearch('white')"
    />
    <div class="search-results mt-4">
      <div
        v-for="user in whiteSearch.results"
        :key="user.userId"
        class="search-item p-2 hover:bg-gray-100 cursor-pointer"
        @click="addUserToList(user, 'white', 'white')"
      >
        {{ user.userName }} (ID: {{ user.userId }})
      </div>
    </div>
  </el-dialog>

  <!-- 黑名单搜索弹窗 -->
  <el-dialog v-model="blackSearch.visible" title="添加黑名单用户" width="30%">
    <el-input
      v-model="blackSearch.query"
      placeholder="输入用户名搜索"
      @input="handleUserSearch('black')"
    />
    <div class="search-results mt-4">
      <div
        v-for="user in blackSearch.results"
        :key="user.userId"
        class="search-item p-2 hover:bg-gray-100 cursor-pointer"
        @click="addUserToList(user, 'black', 'black')"
      >
        {{ user.userName }} (ID: {{ user.userId }})
      </div>
    </div>
  </el-dialog>
</template>

<style scoped>
.el-tag + .el-tag {
  margin-left: 10px;
}

.button-new-tag {
  padding-top: 0;
  padding-bottom: 0;
}

.input-new-tag {
  width: 120px;
  margin-left: 10px;
  vertical-align: bottom;
}

.mb-4 {
  margin-bottom: 16px;
}

/* 调整弹窗样式 */
:deep(.el-dialog__body) {
  padding: 10px 20px;
}
.search-results {
  max-height: 300px;
  overflow-y: auto;
}

.user-list {
  gap: 8px;
}

/* 统一所有标签的间距 */
.el-tag {
  margin: 0 !important;
}
</style>
