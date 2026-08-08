<template>
  <el-card class="thread-list">
    <div v-if="loading" class="pt-5 pl-3 pr-3">
      <el-skeleton :rows="2" animated />
    </div>
    <div v-else-if="forumPostPage.errMsg">
      <el-empty :description="forumPostPage.errMsg" />
    </div>
    <div v-else>
      <el-empty description="暂无内容" v-if="forumPostPage.records?.length === 0" />
      <div
        v-for="thread in forumPostPage.records"
        :key="thread.threadId"
        class="thread-item flex gap-4 pl-2 pr-2 sm:pl-5 sm:pr-5 pt-3 pb-2 transition-colors duration-200 hover:bg-[#f2f3f5]"
      >
        <div class="flex-1 min-w-0">
          <a :href="`/t/${thread.threadId}`" target="_blank" class="block">
            <div class="item-title" v-if="thread.subject">
              <div class="title-text">
                <span v-if="thread.top" class="item-badge badge-top">置顶</span>
                <span v-if="thread.digest" class="item-badge badge-digest">精华</span>
                <span v-if="thread.closed" class="item-badge badge-closed">关闭</span>{{ thread.subject }}
              </div>
            </div>
            <div v-if="thread.brief" class="item-brief">
              <el-text line-clamp="1" style="color: #8a919f; font-size: 13px; line-height: 22px">
                {{ thread.brief }}
              </el-text>
            </div>
          </a>
          <div class="item-meta">
            <div class="flex flex-wrap gap-2 items-center">
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
                @click.stop="goUserHome(thread.authorId)"
              >
                {{ thread.authorName }}
              </div>
              <div class="flex items-center justify-center">
                <el-icon>
                  <ChatDotRound />
                </el-icon>
                <div style="margin-left: 2px">{{ thread.comments }}</div>
              </div>
              <div class="flex items-center justify-center">
                <el-icon>
                  <Icon name="tabler:thumb-up" />
                </el-icon>
                <div style="margin-left: 2px">{{ thread.likes }}</div>
              </div>
              <div class="text-gray-200">|</div>
              <div>{{ formatTimeAgo(thread.displayTime || thread.createTime) }}</div>
            </div>
          </div>
        </div>
        <!-- 文章缩略图 -->
        <a
          v-if="thread.images?.length"
          :href="`/t/${thread.threadId}`"
          target="_blank"
          class="item-thumb flex-shrink-0"
        >
          <img :src="thread.images[0]" :alt="thread.subject" loading="lazy" @error="onThumbError" />
        </a>
      </div>
      <!-- 加载状态提示 -->
      <div v-if="disableLoadMore" class="text-center py-2 text-gray-500 text-sm">
        加载中...
      </div>
      <!-- 没有更多数据提示 -->
      <div v-else-if="!forumPostPage.hasNext && forumPostPage.records?.length > 0" class="text-center py-3 text-gray-400 text-sm">
        已经到底啦~
      </div>
    </div>
  </el-card>
</template>
<script setup lang="ts">
import { ChatDotRound } from '@element-plus/icons-vue'
import type { Thread } from '~/types/global'
import { useAutoLoadMore } from '~/composables/useAutoLoadMore'
const { formatTimeAgo } = useTimeFormat()

const props = defineProps({
  disableLoadMore: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  },
  forumPostPage: {
    type: Object,
    default: () => {
      return {
        records: [],
        hasNext: false,
        total: 0,
        current: 0,
        size: 0,
        errMsg: undefined
      }
    }
  }
})

const emits = defineEmits(['loadMoreThreads'])
const loadMoreThreads = () => {
  emits('loadMoreThreads')
}

// 使用自动加载更多 composable
const useAutoLoadResult = useAutoLoadMore({
  disabled: computed(() => props.disableLoadMore),
  hasMore: computed(() => props.forumPostPage.hasNext),
  onLoad: loadMoreThreads,
  distance: 200,
  mobileOnly: false
})
const { isMobile } = useAutoLoadResult
const clickThread = (thread: Thread) => {
  if (thread?.threadId) {
    window.open('/t/' + thread.threadId, '_blank')
  }
}
const goUserHome = (userId: number) => {
  window.open(`/user/${userId}`, '_blank')
}
const goForum = (forumName: string) => {
  navigateTo(`/f/${forumName}`)
}
// 缩略图加载失败时显示默认占位图
const onThumbError = (e: Event) => {
  const img = e.target as HTMLImageElement
  // 防止默认图加载失败时触发死循环
  img.onerror = null
  img.src = '/img/404.png'
}
</script>

<style lang="scss" scoped>
.thread-item {
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.title-text {
  color: #252933;
  font-size: 16px;
  font-weight: 600;
  line-height: 24px;
  overflow-wrap: break-word;
  transition: color 0.2s;

  a:hover & {
    color: #1e80ff;
  }
}

.item-badge {
  display: inline-flex;
  align-items: center;
  height: 20px;
  padding: 0 6px;
  margin-right: 6px;
  font-size: 12px;
  font-weight: 500;
  line-height: 1;
  border-radius: 4px;
  position: relative;
  // 与标题文字视觉居中对齐
  top: -1px;
}

.badge-top {
  color: #f56c6c;
  background-color: rgba(245, 108, 108, 0.1);
}

.badge-digest {
  color: #67c23a;
  background-color: rgba(103, 194, 58, 0.12);
}

.badge-closed {
  color: #909399;
  background-color: rgba(144, 147, 153, 0.12);
}

.item-brief {
  margin-top: 2px;
}

.item-meta {
  margin-top: 6px;
  color: #8a919f;
  font-size: 13px;
  line-height: 22px;
}

.item-thumb {
  display: block;
  width: 120px;
  height: 80px;
  margin-top: 2px;
  border-radius: 6px;
  overflow: hidden;
  background-color: #f2f3f5;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s;
  }

  &:hover img {
    transform: scale(1.05);
  }
}

@media screen and (max-width: 768px) {
  .item-thumb {
    width: 92px;
    height: 62px;
  }
}
</style>
