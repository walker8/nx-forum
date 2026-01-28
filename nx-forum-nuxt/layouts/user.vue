<template>
  <el-container class="forum-user-layout">
    <el-header class="p-0 rn-hide">
      <common-header />
    </el-header>
    <div class="h-full mx-auto mt-2.5 w-full sm:w-fit">
      <div v-if="errorMsg">
        <el-empty :description="errorMsg" />
      </div>
      <div v-else>
        <el-card class="md:mr-0 md:w-[900px] mb-5 rn-hide">
          <div class="w-full">
            <div class="flex w-full">
              <el-avatar
                :src="userInfo.avatar"
                class="mr-2.5 flex-shrink-0"
                shape="square"
                :size="100"
              />
              <div class="flex-grow min-w-0">
                <div class="flex items-center justify-between mb-1.5 w-full">
                  <el-text size="large" tag="b" :line-clamp="1">
                    {{ userInfo.userName }}
                  </el-text>
                  <el-button
                    v-if="showFollowBtn"
                    :type="userInfo.followed ? 'default' : 'primary'"
                    size="small"
                    class="ml-2.5 flex-shrink-0 max-md:text-xs max-md:px-2.5 max-md:py-1.5"
                    @click="handleFollowClick"
                    :loading="followLoading"
                  >
                    {{ userInfo.followed ? '取消关注' : '关注' }}
                  </el-button>
                </div>
                <el-text type="info" :line-clamp="2" class="block w-full">
                  {{ userInfo.intro }}
                </el-text>
                <el-text type="info" :line-clamp="1" class="block mt-1.5 w-full">
                  <span class="inline-flex items-center">
                    <Icon name="tabler:calendar" class="mr-1 w-4 h-4" />
                    注册于 {{ userInfo.createTime }}
                  </span>
                  <span class="hidden md:inline-flex items-center ml-4">
                    <Icon name="tabler:clock" class="mr-1 w-4 h-4" />
                    活跃于 {{ userInfo.lastActiveDate }}
                  </span>
                </el-text>
              </div>
            </div>
          </div>
        </el-card>
        <el-container class="max-w-[900px] md:w-[900px]">
          <el-aside width="160px" class="hidden-md-and-down mr-5">
            <div class="w-full border border-gray-200 rounded-md bg-white">
              <div class="flex flex-col">
                <button
                  v-for="menu in userHome.tabs"
                  :key="menu.value"
                  :class="[
                    'px-4 py-3 text-sm font-normal whitespace-nowrap transition-colors duration-200 relative flex items-center',
                    userHome.selected === menu.value
                      ? 'text-[#409eff] font-medium bg-blue-50'
                      : 'text-gray-600 hover:text-gray-800 hover:bg-gray-50'
                  ]"
                  @click="goUserTabs(menu.value)"
                >
                  <div class="flex items-center justify-center mr-3" v-if="menu.icon">
                    <Icon :name="menu.icon" class="w-5 h-5" />
                  </div>
                  <span>{{ menu.label }}</span>
                  <div
                    v-if="userHome.selected === menu.value"
                    class="absolute left-0 top-0 h-full w-1 bg-[#409eff]"
                  ></div>
                </button>
              </div>
            </div>
          </el-aside>
          <el-main class="pt-0 max-md:w-[calc(100vw-40px)] max-md:p-0">
            <div class="w-full mb-1.5 hidden-lg-and-up">
              <van-tabs v-model:active="userHome.selected" sticky shrink @click-tab="onClickTab">
                <van-tab v-for="menu in userHome.tabs" :title="menu.label" :name="menu.value" />
              </van-tabs>
            </div>
            <slot />
          </el-main>
        </el-container>
      </div>
    </div>
  </el-container>
</template>
<script setup lang="ts">
import { getForumUserInfo } from '~/apis/forum'
import { followUser, unfollowUser } from '~/apis/follow'

const route = useRoute()
const userHome = useUserHome()
if (route.path.endsWith('comment')) {
  userHome.value.selected = 'comment'
} else if (route.path.endsWith('like')) {
  userHome.value.selected = 'like'
} else if (route.path.endsWith('favorite')) {
  userHome.value.selected = 'favorite'
} else {
  userHome.value.selected = ''
}
const userInfo = useUserInfo()
const currentUser = useUser()
const errorMsg = ref('')
const followLoading = ref(false)

// 判断是否显示关注按钮（不能关注自己）
const showFollowBtn = computed(() => {
  return (
    userInfo.value.userId > 0 &&
    currentUser.value.userId > 0 &&
    userInfo.value.userId !== currentUser.value.userId
  )
})

// 处理关注/取消关注点击事件
const handleFollowClick = async () => {
  if (!currentUser.value.userId) {
    // 未登录，跳转到登录页
    navigateTo('/login')
    return
  }

  followLoading.value = true
  try {
    if (userInfo.value.followed) {
      // 取消关注
      await unfollowUser(userInfo.value.userId)
    } else {
      // 关注
      await followUser({ followUserId: userInfo.value.userId })
    }
    // 更新关注状态
    userInfo.value.followed = !userInfo.value.followed
  } catch (error) {
    console.error('关注操作失败', error)
  } finally {
    followLoading.value = false
  }
}

watch(
  () => route.params.userId,
  (to) => {
    // navigateTo 获取不到路由参数，需要通过userInfo传进来
    let userId = Number(to || userInfo.value.userId)
    if (isNaN(userId) || userId <= 0) {
      errorMsg.value = '用户不存在'
    } else {
      getForumUserInfo(userId)
        .then((res) => {
          const data = res.data
          userInfo.value = data
          userInfo.value.avatar = userInfo.value.avatar || '/img/avatar.png'
        })
        .catch((err) => {
          errorMsg.value = err.message || '获取用户信息失败'
        })
    }
  },
  { flush: 'pre', immediate: true, deep: true }
)
const onClickTab = ({ name }: { name: string }) => {
  goUserTabs(name)
}
const goUserTabs = (name: string) => {
  userHome.value.selected = name
  if (name) {
    navigateTo(`/user/${userInfo.value.userId}/${name}`)
  } else {
    navigateTo(`/user/${userInfo.value.userId}`)
  }
}
</script>
