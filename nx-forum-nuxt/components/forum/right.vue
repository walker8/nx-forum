<template>
  <div class="w-67.5">
    <el-card v-if="forumInfo.brief" class="mb-4">
      <div class="text-gray-500 text-sm font-normal leading-6" v-html="forumInfo.brief" />
    </el-card>
    <el-card v-if="hotPosts.length >= 3" class="article-hot">
      <template #header>
        <div class="card-header flex items-center justify-between">
          <span>文章榜</span>
        </div>
      </template>
      <div class="hot-posts">
        <div
          v-for="(post, index) in hotPosts"
          :key="post.threadId"
          @click="go(`/t/${post.threadId}`)"
          class="post-item flex items-start py-2 px-3 hover:bg-gray-100 cursor-pointer"
        >
          <span class="rank-num mr-2" :class="{ 'top-3': index < 3 }">{{ index + 1 }}</span>
          <div class="post-title flex-1 text-sm text-gray-700 truncate">
            {{ post.subject }}
          </div>
        </div>
      </div>
    </el-card>
    <el-card class="mt-4">
      <template #header>
        <div class="card-header flex items-center justify-between">
          <span>RSS 订阅</span>
        </div>
      </template>
      <div
        @click="goToRss"
        class="cursor-pointer text-[#8a919f] flex items-center justify-center h-[48px] hover:bg-gray-100 rounded-md"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="24"
          height="24"
          fill="currentColor"
          class="bi bi-rss"
          viewBox="0 0 16 16"
        >
          <path
            d="M14 1a1 1 0 0 1 1 1v12a1 1 0 0 1-1 1H2a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1zM2 0a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V2a2 2 0 0 0-2-2z"
          />
          <path
            d="M5.5 12a1.5 1.5 0 1 1-3 0 1.5 1.5 0 0 1 3 0m-3-8.5a1 1 0 0 1 1-1c5.523 0 10 4.477 10 10a1 1 0 1 1-2 0 8 8 0 0 0-8-8 1 1 0 0 1-1-1m0 4a1 1 0 0 1 1-1 6 6 0 0 1 6 6 1 1 0 1 1-2 0 4 4 0 0 0-4-4 1 1 0 0 1-1-1"
          />
        </svg>
        <span class="ml-2 text-sm">订阅{{ forumInfo.nickName || '本' }}版块文章</span>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { type Thread } from '~/types/global'
import { getHotPosts as getHotPostsApi } from '~/apis/thread'

const forumInfo = useForumInfo()

const hotPosts = ref<Thread[]>([])

const getHotPosts = async () => {
  try {
    const { data } = await getHotPostsApi({
      days: 14,
      limit: 8
    })
    hotPosts.value = data
  } catch (error) {
    console.error('获取热门文章失败:', error)
  }
}

const go = (url: string) => {
  window.open(url, '_blank')
}

const goToRss = () => {
  if (forumInfo.value && forumInfo.value.name) {
    navigateTo(`/nx-forum/v1/rss/forum/${forumInfo.value.name}`, { external: true })
  }
}

onMounted(() => {
  nextTick(() => {
    getHotPosts()
  })
})
</script>
