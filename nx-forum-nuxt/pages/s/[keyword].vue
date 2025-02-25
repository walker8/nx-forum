<template>
  <el-card class="thread-list pb-4">
    <div v-if="forumPostPage.errMsg">
      <el-empty :description="forumPostPage.errMsg" />
    </div>
    <div v-else>
      <el-empty description="抱歉，没有找到相关内容" v-if="forumPostPage.records?.length === 0" />
      <div
        v-for="thread in forumPostPage.records"
        @click="clickThread(thread)"
        class="hover:bg-[#f2f3f5] hover:cursor-pointer pl-2 pr-2 sm:pl-5 sm:pr-5"
      >
        <div style="padding-top: 12px">
          <div class="flex flex-wrap gap-2" v-if="thread.subject">
            <span
              style="
                color: #252933;
                font-size: 16px;
                font-weight: 600;
                line-height: 24px;
                display: block;
                max-width: 100%;
              "
              v-html="thread.subject"
            />
          </div>
          <div v-if="thread.brief">
            <span
              style="
                color: #8a919f;
                font-size: 13px;
                line-height: 22px;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
                display: block;
                max-width: 100%;
              "
              v-html="thread.brief"
            />
          </div>
          <div style="color: #8a919f; font-size: 13px; line-height: 22px">
            <div class="flex flex-wrap items-center gap-2">
              <el-tag
                type="info"
                size="small"
                class="cursor-pointer hover:text-[#409eff] hover:bg-sky-100"
                @click.stop="goForum(thread.forumName)"
              >
                {{ thread.forumNickName }}
              </el-tag>
              <div
                class="max-w-25 overflow-hidden text-ellipsis whitespace-nowrap cursor-pointer hover:text-[#409eff]"
                @click.stop="goUserHome(thread.author?.authorId)"
              >
                {{ thread.author?.authorName }}
              </div>
              <div class="text-gray-200">|</div>
              <div>{{ thread.createTime }}</div>
            </div>
          </div>
        </div>
        <el-divider style="margin: 0px; margin-top: 8px" />
      </div>
      <common-load-more
        :has-next="forumPostPage.hasNext"
        :disable-load-more="disableLoadMore"
        @load-more="loadMoreThreads"
        class="mr-4 ml-4 pt-1"
      >
        加载更多内容
      </common-load-more>
    </div>
  </el-card>
</template>
<script setup lang="ts">
import { queryThreadsByKeyword, type ThreadQuery } from '~/apis/thread'
import type { Thread } from '~/types/global'
import { ElMessage } from 'element-plus'

definePageMeta({
  layout: 'search'
})

const forumPostPage = useForumPostPage()
const disableLoadMore = ref(false)
const route = useRoute()
const keyword = route.params.keyword as string
let threadQuery: ThreadQuery = {
  authorName: '',
  keyword: keyword,
  orderBy: 'last_comment_time',
  pageNo: 1,
  pageSize: 15
}
queryThreadsByKeyword(threadQuery).then((res) => {
  forumPostPage.value = res.data
})
const loadMoreThreads = () => {
  disableLoadMore.value = true
  const pageNo = forumPostPage.value.current + 1
  threadQuery.pageNo = pageNo
  queryThreadsByKeyword(threadQuery)
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
const clickThread = (thread: Thread) => {
  if (thread?.threadId) {
    window.open('/t/' + thread.threadId, '_blank')
  }
}
const goUserHome = (userId: number | null | undefined) => {
  if (userId) {
    window.open(`/user/${userId}`, '_blank')
  }
}
const goForum = (forumName: string) => {
  navigateTo(`/f/${forumName}`)
}

useSeoMeta({
  title: () => `搜索结果-${keyword}`
})
</script>
