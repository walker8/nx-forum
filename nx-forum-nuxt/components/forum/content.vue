<template>
  <thread-list
    :forum-post-page="forumPostPage"
    :disable-load-more="disableLoadMore"
    :loading="forumPostPage.loading"
    @load-more-threads="loadMoreThreads"
  />
</template>
<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { queryThreads, type ThreadQuery } from '~/apis/thread'
const forumPostPage = useForumPostPage()
const route = useRoute()
const forumName = route.params.forumName ? String(route.params.forumName) : ''
const disableLoadMore = ref(false)
let threadQuery: ThreadQuery = {
  forumName: forumName,
  authorName: '',
  keyword: '',
  orderBy: 'last_comment_time',
  pageNo: 1,
  pageSize: forumPostPage.value.size
}
const loadMoreThreads = () => {
  disableLoadMore.value = true
  const pageNo = forumPostPage.value.current + 1
  threadQuery.pageNo = pageNo
  queryThreads(threadQuery)
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
