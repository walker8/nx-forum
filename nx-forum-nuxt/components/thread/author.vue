<template>
  <el-card class="mb-4 author-card">
    <template #header>
      <div class="flex items-center">
        <span>作者信息</span>
      </div>
    </template>
    <div class="p-4">
      <div class="flex gap-4 mb-4 items-center">
        <div class="flex-shrink-0">
          <el-avatar :size="60" :src="author?.avatarUrl || '/img/avatar.png'" />
        </div>
        <div class="flex-1">
          <div
            class="text-base font-bold text-[#373a40] hover:text-[#409eff] cursor-pointer"
            @click="goUserHome(author?.authorId)"
          >
            {{ author?.authorName }}
          </div>
          <div class="text-sm text-gray-600 line-clamp-2">
            {{ author?.intro || '暂无介绍' }}
          </div>
        </div>
      </div>
      <div class="flex justify-around py-2.5 mb-4 border-t border-b border-gray-100">
        <div class="text-center">
          <div class="text-base font-bold text-gray-800">
            {{ author?.threads || 0 }}
          </div>
          <div class="text-xs text-gray-600 mt-0.5">文章</div>
        </div>
        <div class="text-center">
          <div class="text-base font-bold text-gray-800">
            {{ author?.comments || 0 }}
          </div>
          <div class="text-xs text-gray-600 mt-0.5">评论</div>
        </div>
        <div class="text-center">
          <div class="text-base font-bold text-gray-800">
            {{ fans }}
          </div>
          <div class="text-xs text-gray-600 mt-0.5">粉丝</div>
        </div>
      </div>
      <div class="flex gap-2.5">
        <el-button
          :type="isFollowed ? 'default' : 'primary'"
          class="flex-1 h-8"
          :loading="loading"
          @click="handleFollow"
        >
          {{ isFollowed ? '取消关注' : '关注' }}
        </el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import type { Author } from '~/types/global'
import { followUser, unfollowUser } from '~/apis/follow'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  author?: Author
}>()

const { user } = useCurrentUser()
const isFollowed = ref(props.author?.followed || false)
const loading = ref(false)
const fans = ref(props.author?.fans || 0)
// 关注/取消关注
const handleFollow = async () => {
  if (!props.author?.authorId) return
  if (!user.value.userId) {
    ElMessage.warning('请先登录')
    return
  }

  loading.value = true
  try {
    if (isFollowed.value) {
      // 取消关注
      const res = await unfollowUser(props.author.authorId)
      if (res.data) {
        isFollowed.value = false
        ElMessage.success('已取消关注')
        // 更新粉丝数
        if (fans.value && fans.value > 0) {
          fans.value--
        }
      }
    } else {
      // 关注
      const res = await followUser({ followUserId: props.author.authorId })
      if (res.data) {
        isFollowed.value = true
        ElMessage.success('关注成功')
        // 更新粉丝数
        if (fans.value !== null) {
          fans.value++
        }
      }
    }
  } catch (error) {
    console.error('关注操作失败', error)
    ElMessage.error(error || '操作失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const goUserHome = (userId: number) => {
  const userInfo = useUserInfo()
  userInfo.value.userId = userId
  const userHome = useUserHome()
  userHome.value.selected = ''
  navigateTo(`/user/${userId}`)
}
</script>
