<template>
  <ClientOnly>
    <div class="mb-4">
      <el-card>
        <el-empty v-if="followerList.length === 0" description="暂无粉丝" />
        <div v-else v-for="item in followerList" :key="item.userId" class="mb-4">
          <div class="flex items-center justify-between">
            <div class="flex items-center">
              <el-avatar :size="50" :src="item.avatar || '/img/avatar.png'" class="mr-4" />
              <div>
                <div
                  class="text-base font-bold hover:text-primary cursor-pointer"
                  @click="navigateToUser(item.userId)"
                >
                  {{ item.userName }}
                </div>
                <div class="text-sm text-gray-500 mt-1 line-clamp-1">
                  {{ item.intro || '暂无介绍' }}
                </div>
                <div class="text-xs text-gray-400 mt-1">关注时间: {{ item.followTime }}</div>
              </div>
            </div>
            <div>
              <el-button
                v-if="!item.followed"
                type="primary"
                size="small"
                :loading="followLoading === item.userId"
                @click="handleFollow(item.userId)"
              >
                关注
              </el-button>
              <el-button
                v-else
                type="default"
                size="small"
                :loading="followLoading === item.userId"
                @click="handleUnfollow(item.userId)"
              >
                已关注
              </el-button>
            </div>
          </div>
        </div>
        <div class="flex justify-end mt-4">
          <el-pagination
            v-if="total > 0"
            v-model:current-page="query.pageNo"
            v-model:page-size="query.pageSize"
            :page-sizes="[10, 20, 30, 50]"
            layout="total, sizes, prev, pager, next"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>
  </ClientOnly>
</template>

<script setup lang="ts">
import { getFollowerList, followUser, unfollowUser } from '~/apis/follow'
import type { UserFollowVO } from '~/apis/follow'
import { ElMessage } from 'element-plus'

definePageMeta({
  layout: 'user'
})
useSeoMeta({
  title: '粉丝列表'
})

const route = useRoute()
const userId = computed(() => Number(route.params.userId))
const userHome = useUserHome()
userHome.value.selected = 'followers'

const { user } = useCurrentUser()
const isLoggedIn = computed(() => !!user.value.userId)

const followerList = ref<UserFollowVO[]>([])
const total = ref(0)
const followLoading = ref<number | null>(null)

const query = reactive({
  pageNo: 1,
  pageSize: 10
})

// 获取粉丝列表
const fetchFollowerList = async () => {
  try {
    const res = await getFollowerList(userId.value, query)
    followerList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取粉丝列表失败', error)
    ElMessage.error('获取粉丝列表失败')
  }
}

// 关注用户
const handleFollow = async (followUserId: number) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    return
  }

  followLoading.value = followUserId
  try {
    const res = await followUser({ followUserId })
    if (res.data) {
      ElMessage.success('关注成功')
      // 更新关注状态
      const follower = followerList.value.find((item) => item.userId === followUserId)
      if (follower) {
        follower.followed = true
      }
    }
  } catch (error) {
    console.error('关注失败', error)
    ElMessage.error(error || '操作失败，请稍后重试')
  } finally {
    followLoading.value = null
  }
}

// 取消关注
const handleUnfollow = async (followUserId: number) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    return
  }

  followLoading.value = followUserId
  try {
    const res = await unfollowUser(followUserId)
    if (res.data) {
      ElMessage.success('已取消关注')
      // 更新关注状态
      const follower = followerList.value.find((item) => item.userId === followUserId)
      if (follower) {
        follower.followed = false
      }
    }
  } catch (error) {
    console.error('取消关注失败', error)
    ElMessage.error(error || '操作失败，请稍后重试')
  } finally {
    followLoading.value = null
  }
}

// 分页处理
const handleSizeChange = (val: number) => {
  query.pageSize = val
  query.pageNo = 1
  fetchFollowerList()
}

const handleCurrentChange = (val: number) => {
  query.pageNo = val
  fetchFollowerList()
}

// 跳转到用户页面
const navigateToUser = (userId: number) => {
  navigateTo(`/user/${userId}`)
}

// 初始化
fetchFollowerList()

// 监听路由参数变化
watch(
  () => route.params.userId,
  () => {
    fetchFollowerList()
  }
)
</script>
