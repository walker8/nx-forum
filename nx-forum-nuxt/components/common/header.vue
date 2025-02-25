<template>
  <div class="h-[55px] bg-white">
    <div class="flex items-center h-[55px] mx-auto max-w-[1200px] px-[10px]">
      <div class="cursor-pointer" @click="goHome">
        <CommonLogo />
      </div>
      <div class="flex items-center justify-end w-full">
        <client-only>
          <div class="flex flex-wrap items-center gap-4">
            <div :class="{ 'hidden sm:block': type != 'search' }">
              <el-input
                v-model="keyword"
                style="max-width: 600px"
                placeholder="搜索文章"
                @keydown.enter="handleSearch"
              >
                <template #append>
                  <el-button :icon="Search" @click="handleSearch" />
                </template>
              </el-input>
            </div>
            <div v-if="!user.userName" class="hidden sm:flex space-x-2 ml-2 text-[#8a919f]">
              <div class="cursor-pointer" @click="go('/uc/login')">登录</div>
              <div
                class="cursor-pointer"
                @click="go('/uc/register')"
                v-if="authConfig?.registerConfig?.enableRegister"
              >
                注册
              </div>
            </div>
            <el-button type="primary" @click="post" v-if="user.userName && type != 'search'">
              发布文章
            </el-button>
            <div class="hidden sm:flex gap-2">
              <el-dropdown v-if="user.userName">
                <div class="cursor-pointer text-[#8a919f]">
                  <el-badge
                    :value="userNotification.totalCount"
                    :offset="[-5, 5]"
                    :show-zero="false"
                  >
                    <el-icon :size="30"><BellFilled /></el-icon>
                  </el-badge>
                </div>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="go('/notification')">
                      回复消息
                      <el-tag
                        type="danger"
                        effect="dark"
                        size="small"
                        class="ml-2"
                        v-if="userNotification.replyCount > 0"
                      >
                        {{ userNotification.replyCount }}
                      </el-tag>
                    </el-dropdown-item>
                    <el-dropdown-item @click="go('/notification/mention')">
                      @消息
                      <el-tag
                        type="danger"
                        effect="dark"
                        size="small"
                        class="ml-2"
                        v-if="userNotification.mentionCount > 0"
                      >
                        {{ userNotification.mentionCount }}
                      </el-tag>
                    </el-dropdown-item>
                    <el-dropdown-item @click="go('/notification/system')">
                      系统消息
                      <el-tag
                        type="danger"
                        effect="dark"
                        size="small"
                        class="ml-2"
                        v-if="userNotification.systemCount > 0"
                      >
                        {{ userNotification.systemCount }}
                      </el-tag>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-dropdown v-show="user.userName">
                <div class="cursor-pointer text-primary flex items-center">
                  <el-avatar :size="30" :src="user.avatar" />
                  <el-icon class="el-icon--right">
                    <arrow-down />
                  </el-icon>
                </div>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item disabled>{{ user.userName }}</el-dropdown-item>
                    <el-dropdown-item divided @click="goUserHome(user.userId)">
                      我的主页
                    </el-dropdown-item>
                    <el-dropdown-item @click="go('/uc/settings/profile')">
                      个人设置
                    </el-dropdown-item>
                    <el-dropdown-item @click="go('/admin')" v-if="hasPermission('admin:manage')">
                      后台管理
                    </el-dropdown-item>
                    <el-dropdown-item divided @click="handleLogout"> 退出登录 </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </client-only>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ArrowDown, Search, BellFilled } from '@element-plus/icons-vue'
import { getNotificationCount } from '~/apis/notification'

const props = defineProps({
  //类型
  type: {
    type: String,
    default: 'default'
  }
})
const { type } = props
const go = (path: string) => {
  navigateTo(`${path}`)
}
const goUserHome = (userId: number) => {
  const userInfo = useUserInfo()
  userInfo.value.userId = userId
  const userHome = useUserHome()
  userHome.value.selected = ''
  navigateTo(
    {
      path: `/user/${userId}`
    },
    {
      replace: true
    }
  )
}
const keyword = ref('')

const handleSearch = () => {
  if (keyword.value) {
    navigateTo(`/s/${keyword.value}`)
  }
}
const goHome = () => {
  window.open('/', '_self')
}
const post = () => {
  navigateTo({
    path: '/editor/new'
  })
}
const { user, handleLogout } = useCurrentUser()
const userNotification = useUserNotificationCount()
const { hasPermission, authPromise } = useUserAuth()
await authPromise
// 获取通知数量
const fetchNotificationCount = async () => {
  try {
    const res = await getNotificationCount()
    const data = res.data
    userNotification.value = {
      mentionCount: data.mentionCount,
      replyCount: data.replyCount,
      systemCount: data.systemCount,
      totalCount: data.totalCount
    }
  } catch (error) {
    console.error('获取通知数量失败:', error)
  }
}

const authConfig = ref()
onMounted(async () => {
  const res = await useAuthConfig()
  authConfig.value = res.value
})

// 监听用户ID变化
watch(
  () => user.value.userId,
  (userId) => {
    if (userId && userId > 0) {
      fetchNotificationCount()
    } else {
      // 用户未登录或登出时，清空通知计数
      userNotification.value = {
        mentionCount: 0,
        replyCount: 0,
        systemCount: 0,
        totalCount: 0
      }
    }
  },
  { immediate: true }
)
</script>
