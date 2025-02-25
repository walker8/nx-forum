<template>
  <thread-list
    :forum-post-page="forumPostPage"
    :disable-load-more="disableLoadMore"
    @load-more-threads="loadMoreThreads"
  />
</template>
<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { queryThreadsByUserId, type ThreadQuery } from '~/apis/thread'
definePageMeta({
  layout: 'user'
})
useSeoMeta({
  title: '个人主页'
})
const forumPostPage = useForumPostPage()
const route = useRoute()
const userId = route.params.userId
const disableLoadMore = ref(false)
let threadQuery: ThreadQuery = {
  pageNo: 1,
  pageSize: 15
}
if (userId) {
  // 查询帖子列表
  try {
    // 获取用户信息
    const res = await queryThreadsByUserId(Number(userId), threadQuery)
    // 获取列表成功
    let data = res.data
    forumPostPage.value = data
    forumPostPage.value.errMsg = ''
  } catch (errMsg) {
    // 获取列表失败
    forumPostPage.value.errMsg = String(errMsg)
  }
}
const loadMoreThreads = () => {
  disableLoadMore.value = true
  const pageNo = forumPostPage.value.current + 1
  threadQuery.pageNo = pageNo
  queryThreadsByUserId(Number(userId), threadQuery)
    .then((res) => {
      const data = res.data
      forumPostPage.value.records.push(...data.records)
      forumPostPage.value.hasNext = data.hasNext
      forumPostPage.value.total = data.total
      forumPostPage.value.current = data.current
      forumPostPage.value.size = data.size
    })
    .catch((err) => {
      ElMessage.error(err)
    })
    .finally(() => {
      disableLoadMore.value = false
    })
}
</script>
