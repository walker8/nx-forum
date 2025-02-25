<template>
  <ClientOnly>
    <div class="mb-4">
      <el-card>
        <el-empty v-if="followingList.length === 0" description="暂无关注的用户" />
        <div v-else v-for="item in followingList" :key="item.userId" class="mb-4">
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
                v-if="isCurrentUser"
                type="danger"
                size="small"
                plain
                :loading="unfollowLoading === item.userId"
                @click="handleUnfollow(item.userId)"
              >
                取消关注
              </el-button>
            </div>
          </div>
        </div>

        <div class="flex justify-end mt-4">
          <el-pagination
            background
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
import { getFollowingList, unfollowUser } from '~/apis/follow'
import type { UserFollowVO } from '~/apis/follow'
import { ElMessage, ElMessageBox } from 'element-plus'
definePageMeta({
  layout: 'user'
})
useSeoMeta({
  title: '关注列表'
})

const route = useRoute()
const userId = computed(() => Number(route.params.userId))
const userHome = useUserHome()
userHome.value.selected = 'following'

const { user } = useCurrentUser()
const isCurrentUser = computed(() => user.value.userId === userId.value)

const followingList = ref<UserFollowVO[]>([])
const total = ref(0)
const unfollowLoading = ref<number | null>(null)

const query = reactive({
  pageNo: 1,
  pageSize: 10
})

// 获取关注列表
const fetchFollowingList = () => {
  getFollowingList(userId.value, query)
    .then((res) => {
      followingList.value = res.data.records
      total.value = res.data.total
    })
    .catch((error) => {
      ElMessage.error(error || '获取关注列表失败')
    })
}

// 取消关注
const handleUnfollow = async (followUserId: number) => {
  if (!isCurrentUser.value) {
    ElMessage.warning('只能操作自己的关注列表')
    return
  }

  ElMessageBox.confirm('确定要取消关注该用户吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      unfollowLoading.value = followUserId
      const res = await unfollowUser(followUserId)
      if (res.data) {
        ElMessage.success('已取消关注')
        // 重新获取关注列表
        fetchFollowingList()
      }
    } catch (error) {
      ElMessage.error(error || '取消关注失败')
    } finally {
      unfollowLoading.value = null
    }
  })
}

// 分页处理
const handleSizeChange = (val: number) => {
  query.pageSize = val
  query.pageNo = 1
  fetchFollowingList()
}

const handleCurrentChange = (val: number) => {
  query.pageNo = val
  fetchFollowingList()
}

// 跳转到用户页面
const navigateToUser = (userId: number) => {
  navigateTo(`/user/${userId}`)
}

// 初始化
fetchFollowingList()

// 监听路由参数变化
watch(
  () => route.params.userId,
  () => {
    fetchFollowingList()
  }
)
</script>
