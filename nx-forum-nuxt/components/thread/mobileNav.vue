<template>
  <div
    class="fixed bottom-0 left-0 w-full z-50 md:hidden border-t border-gray-200 bg-white/95 backdrop-blur-sm shadow-[0_-4px_12px_rgba(0,0,0,0.08)]"
  >
    <div class="flex justify-around items-center py-1.5">
      <div
        class="flex items-center text-[#409eff] cursor-pointer gap-1 px-3 py-1.5 rounded-lg transition-all hover:bg-gray-50 active:scale-95"
        :class="{ 'text-gray-500': !thread.liked }"
        @click="handleLike(thread)"
      >
        <el-icon :size="20">
          <Icon name="tabler:thumb-up" />
        </el-icon>
        <span>{{ thread.likes }}</span>
      </div>
      <div
        class="flex items-center text-[#409eff] cursor-pointer gap-1 px-3 py-1.5 rounded-lg transition-all hover:bg-gray-50 active:scale-95"
        :class="{ 'text-gray-500': !thread.collected }"
        @click="handleFavorite(thread)"
      >
        <el-icon :size="20">
          <Star />
        </el-icon>
        <span>{{ thread.collections }}</span>
      </div>
      <div class="flex items-center text-gray-500 cursor-pointer gap-1 px-3 py-1.5 rounded-lg transition-all hover:bg-gray-50 active:scale-95" @click="goComments">
        <el-icon :size="20">
          <ChatDotRound />
        </el-icon>
        <span>{{ thread.comments }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ChatDotRound, Star } from '@element-plus/icons-vue'
import { toggleLike } from '~/apis/like'
import { ElMessage } from 'element-plus'
import { toggleFavorite } from '~/apis/favorite'

const thread = useThread()

const goComments = () => {
  const element = document.getElementById('comment-all')
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' })
  }
}

const handleLike = async (thread: any) => {
  const user = useUser()
  if (user.value.userId === 0) {
    ElMessage.warning('请先登录再点赞')
    return
  }
  try {
    const targetId = thread.threadId
    const res = await toggleLike({
      targetType: 0,
      targetId
    })

    if (res.data > 0) {
      thread.liked = true
    } else {
      thread.liked = false
    }
    thread.likes = thread.likes + res.data
  } catch (err) {
    ElMessage.error(err || '点赞错误')
  }
}

const handleFavorite = async (thread: any) => {
  const user = useUser()
  if (user.value.userId === 0) {
    ElMessage.warning('请先登录再收藏')
    return
  }
  try {
    const res = await toggleFavorite({
      threadId: thread.threadId,
      isFavorite: !thread.collected
    })

    thread.collected = !thread.collected
    thread.collections += res.data > 0 ? 1 : -1
  } catch (err) {
    ElMessage.error(err || '操作失败')
  }
}
</script>
