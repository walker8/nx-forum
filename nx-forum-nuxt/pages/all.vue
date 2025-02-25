<template>
  <div class="container mx-auto p-4">
    <div v-if="forums.length" class="grid grid-cols-1 sm:grid-cols-2 gap-4">
      <div
        v-for="forum in forums"
        :key="forum.name"
        class="bg-white shadow-md rounded-lg p-4 cursor-pointer hover:bg-gray-200 hover:text-gray-500 transition-colors duration-300"
      >
        <div @click="goForum(forum.name)">
          <div class="text-xl flex items-center gap-2 justify-between">
            <div class="flex items-center gap-2">
              <Icon :name="forum.iconName" class="w-5 h-5" v-if="forum.iconName" />
              <span>{{ forum.nickName }}</span>
            </div>
            <el-button
              v-if="forum.isAdmin"
              type="primary"
              link
              @click.stop="goForumAdmin(forum.forumId)"
            >
              后台管理
            </el-button>
          </div>
          <div class="mt-2">
            <el-text line-clamp="2">{{ forum.shortBrief || '暂无简介' }}</el-text>
          </div>
          <p class="text-gray-500" v-if="forum.threadCount">主题数: {{ forum.threadCount }}</p>
        </div>
      </div>
    </div>
    <div v-else class="text-center">没有找到版块信息</div>
  </div>
</template>

<script lang="ts" setup>
import { getUserForumList } from '@/apis/forum'

definePageMeta({
  layout: 'all'
})

const forums = ref([])

const response = await getUserForumList()
if (response && response.success) {
  forums.value = response.data
} else {
  console.error('获取版块信息失败:', response?.errMessage)
}

const goForum = (name: string) => {
  if (name) {
    navigateTo(`/f/${name}`)
  }
}

const goForumAdmin = (forumId: number) => {
  if (forumId) {
    window.open(`/admin?forumId=${forumId}`, '_blank')
  }
}

useSeoMeta({
  title: '全部版块'
})
</script>
