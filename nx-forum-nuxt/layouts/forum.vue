<template>
  <el-container class="forum-container">
    <div class="content">
      <el-header style="padding: 0" class="hidden sm:block rn-hide">
        <common-header />
      </el-header>
      <div class="common-layout mx-auto sm:mt-2">
        <el-container>
          <el-aside width="200px" class="hidden-md-and-down" v-if="forumMenu.menus?.length > 0">
            <ForumLeft />
          </el-aside>
          <el-main class="forum-main">
            <div class="hidden-lg-and-up m-header forum-menu" v-if="forumMenu.menus?.length > 0">
              <div class="w-full border-b border-gray-10 bg-white">
                <div class="flex overflow-x-auto no-scrollbar">
                  <button v-for="menu in forumMenu.menus" :key="menu.name" :class="[
                    'px-3 py-2 text-sm font-normal whitespace-nowrap transition-colors duration-200 relative',
                    forumMenu.selctedMenu === menu.name
                      ? 'text-[#409eff] font-medium'
                      : 'text-gray-600 hover:text-gray-800'
                  ]" @click="onClickTab({ name: menu.name })">
                    {{ menu.nickName }}
                    <div v-if="forumMenu.selctedMenu === menu.name"
                      class="absolute bottom-0 left-0 w-full h-0.5 bg-[#409eff]"></div>
                  </button>
                </div>
              </div>
            </div>
            <slot />
          </el-main>
          <el-aside class="hidden-sm-and-down">
            <ForumRight />
          </el-aside>
        </el-container>
      </div>
    </div>
    <CommonFooter />
    <CommonTabbar />
  </el-container>
</template>
<script setup lang="ts">
import { useDebounceFn } from '@vueuse/core'
import { getForumInfoByName, getForumShowMenu } from '~/apis/forum'
import { queryThreads, type ThreadQuery } from '~/apis/thread'
const forumPostPage = useForumPostPage()
const forumInfo = useForumInfo()
const forumMenu = useForumMenu()
const route = useRoute()
const user = useUser()

/**
 * 没有访问论坛权限并且未登录转跳登录页面
 * @param forumId
 */
const goLoginPage = async (forumId: number) => {
    if (!user.value.userId || user.value.userId === 0) {
      const { hasPermission, authPromise } = useUserAuth(forumId)
      // 等待权限加载完成后再检查
      await authPromise
      if (!hasPermission('forum:visit', forumId)) {
        navigateTo('/uc/login')
      }
    }
}

let threadQuery: ThreadQuery = {
  forumName: '',
  authorName: '',
  keyword: '',
  orderBy: 'last_comment_time',
  pageNo: 1,
  pageSize: 20
}
forumPostPage.value.loading = false
const queryForumThreads = async (forumName: string, canLoading = false) => {
  forumPostPage.value.errMsg = ''
  // 查询帖子列表
  threadQuery.forumName = forumName
  if (canLoading) {
    forumPostPage.value.loading = true
  }
  try {
    let res = await queryThreads(threadQuery)
    // 获取列表成功
    let data = res.data
    forumPostPage.value = data
  } catch (errMsg) {
    // 获取列表失败 - 服务端和客户端都设置错误消息
    forumPostPage.value.errMsg = String(errMsg)
    // 权限检查只在客户端执行
    if (import.meta.client) {
      await goLoginPage(forumInfo.value.forumId)
    }
  }
  finally {
    if (canLoading) {
      forumPostPage.value.loading = false
    }
  }
}

if (forumMenu.value.selctedMenu !== 'all') {
  // 不是从全部版块页面跳转而来的
  try {
    let res = await getForumShowMenu()
    const data = res.data
    forumMenu.value.selctedMenu = route.params.forumName || data.defaultForumName
    forumMenu.value.menus = data.records
    res = await getForumInfoByName(forumMenu.value.selctedMenu)
    forumInfo.value = res.data
    await queryForumThreads(forumMenu.value.selctedMenu)
  } catch (e) {
    forumPostPage.value.errMsg = String(e)
    if (import.meta.client) {
      setTimeout(() => {
        goLoginPage(forumInfo.value.forumId)
      }, 200)
    }
  }
}

const onClickTab = ({ name }: { name: string }) => {
  if (name === 'all') {
    navigateTo('/all')
  } else {
    navigateTo(`/f/${name}`)
  }
}
watch(
  () => route.params.forumName,
  useDebounceFn((to) => {
    let forumName = ''
    if (to) {
      forumName = String(to)
      forumMenu.value.selctedMenu = forumName
      getForumInfoByName(forumName).then((res) => {
        forumInfo.value = res.data
      })
      queryForumThreads(forumName, true)
    }
  }, 300),
  { flush: 'pre', immediate: false, deep: true }
)

onMounted(() => {
  nextTick(() => {
    goLoginPage(forumInfo.value.forumId)
  })
  watch(
    () => forumInfo.value.forumId,
    (to) => {
      let forumId = 0
      if (to) {
        forumId = Number(to)
      }
      goLoginPage(forumId)
    },
    { flush: 'pre', immediate: false, deep: true }
  )
})
</script>
