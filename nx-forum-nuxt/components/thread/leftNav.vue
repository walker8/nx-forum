<template>
  <div class="fixed z-50 mt-20">
    <el-space direction="vertical" :size="20">
      <el-badge :value="thread.likes" class="item" :type="thread.liked ? 'primary' : 'info'">
        <el-button
          circle
          size="large"
          @click="handleLike(thread)"
          :type="thread.liked ? 'primary' : ''"
        >
          <el-icon :size="18">
            <Icon name="tabler:thumb-up" />
          </el-icon>
        </el-button>
      </el-badge>
      <el-badge
        :value="thread.collections"
        class="item"
        :type="thread.collected ? 'primary' : 'info'"
      >
        <el-button
          circle
          size="large"
          @click="handleFavorite(thread)"
          :type="thread.collected ? 'primary' : ''"
        >
          <el-icon :size="18">
            <Star />
          </el-icon>
        </el-button>
      </el-badge>
      <el-badge :value="thread.comments" class="item" type="info">
        <el-button circle size="large" @click="goComments">
          <el-icon :size="18">
            <ChatDotRound />
          </el-icon>
        </el-button>
      </el-badge>
      <el-button circle size="large" @click="handleReport">
        <el-icon :size="18">
          <WarnTriangleFilled />
        </el-icon>
      </el-button>
    </el-space>
  </div>
</template>
<script setup lang="ts">
import { ChatDotRound, Star, WarnTriangleFilled } from '@element-plus/icons-vue'
import { toggleLike } from '~/apis/like'
import { ElMessage } from 'element-plus'
import { toggleFavorite } from '~/apis/favorite'

const thread = useThread()
const { openReportDialog } = useReport()

const goComments = () => {
  const element = document.getElementById('comment-all')
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' })
  }
}

const handleReport = () => {
  const user = useUser()
  if (user.value.userId === 0) {
    ElMessage.warning('请先登录再举报')
    return
  }
  
  const currentThread = thread.value
  openReportDialog(currentThread.threadId, 'THREAD', currentThread.forumId)
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
