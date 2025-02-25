<template>
  <div class="mt-3" v-if="comment.replies" v-for="(reply, index) in comment.replies" :key="index">
    <div :class="{ 'reply-highlight': replyId && replyId === reply.replyId }">
      <div class="comment-reply">
        <div class="inline-flex">
          <div class="flex items-center">
            <span
              class="font-bold cursor-pointer hover:text-[#409eff]"
              @click="open(`/user/${reply.authorId}`)"
            >
              {{ reply.authorName }}
            </span>
            <van-tag plain type="primary" v-if="reply.authorId == authorId" class="ml-1">
              作者
            </van-tag>
          </div>
          <div v-if="reply.replyAuthorId" style="line-height: 20px" class="flex items-center">
            <span style="margin-right: 6px; margin-left: 6px">回复</span>
            <span class="font-bold cursor-pointer hover:text-[#409eff]">
              {{ reply.replyAuthorName }}
            </span>
            <van-tag plain type="primary" v-if="reply.replyAuthorId == authorId" class="ml-1">
              作者
            </van-tag>
          </div>
          <div>：</div>
        </div>
        <span class="break-words break-all" v-html="replaceEmotions(reply.message)"></span>
      </div>
      <div class="flex text-sm mt-1" style="color: rgb(145, 150, 161)">
        <div>{{ reply.createTime }}</div>
        <div
          :class="{
            'ml-2 flex items-center hover:text-[#409eff] cursor-pointer': true,
            'text-[#409eff]': reply.liked
          }"
          @click="handleLike(reply)"
        >
          <el-icon size="16px" style="position: relative; top: 0px">
            <Icon name="tabler:thumb-up" />
          </el-icon>
          <span class="ml-0.5">{{ reply.likes }}</span>
        </div>
        <div class="ml-2" v-if="hasPermission('comment:new')">
          <div
            @click="reply._showEditor = false"
            v-if="reply._showEditor"
            class="cursor-pointer text-[#409eff]"
          >
            取消回复
          </div>
          <div
            v-else
            @click="!disabled && toReply(reply, comment.authorId)"
            :class="{
              'cursor-not-allowed': disabled,
              'cursor-pointer hover:text-[#409eff]': !disabled
            }"
          >
            回复
          </div>
        </div>
        <div
          v-if="user.userId"
          class="ml-auto cursor-pointer hover:text-[#409eff] pr-1"
          @click="onReportReply(reply)"
        >
          举报
        </div>
      </div>
      <div style="width: 100%; margin-top: 7px" v-if="reply._showEditor">
        <editor-emotion
          :comment-id="comment.commentId"
          :reply-author-id="reply._replyAuthorId"
          :reply-author-name="reply._replyAuthorName"
          :init-focus="true"
          @submit="refreshCommentReplies(comment)"
          :disabled="disabled"
        />
      </div>
    </div>
  </div>
  <div
    class="mt-2"
    v-if="comment.replies && comment.replyCount > comment.replies?.length"
    @click="loadMoreReplies(comment)"
  >
    <el-link type="info" underline="never">
      <span v-if="comment.loading">加载中...</span>
      <span v-else>
        查看全部 {{ comment.replyCount }} 条回复
        <el-icon><ArrowDown /></el-icon>
      </span>
    </el-link>
  </div>
</template>
<script setup lang="ts">
import { queryCommentReplies } from '~/apis/comment'
import { type CommentReplyVO, type CommentVO } from '~/types/global'
import { ArrowDown } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { CommentOrderV } from '~/constant'
import { toggleLike } from '~/apis/like'
import { useReport } from '~/composables/useReport'

// 扩展CommentReplyVO类型，添加UI相关属性
interface ExtendedCommentReplyVO extends CommentReplyVO {
  _showEditor?: boolean
  _replyAuthorId?: number
  _replyAuthorName?: string
}

// 扩展CommentVO类型，支持UI相关属性
interface ExtendedCommentVO {
  commentId: number
  replyCount: number
  images: string[]
  threadId: number
  threadTitle: string
  threadCommentCount: number
  likes: number
  message: string
  createTime: string
  updateTime: string | null
  authorId: number
  authorName: string
  avatarUrl: string
  loading?: boolean
  pageNo?: number
  liked?: boolean
  replies?: ExtendedCommentReplyVO[]
}

const props = defineProps({
  comment: {
    type: Object as () => ExtendedCommentVO,
    required: true
  },
  authorId: {
    type: Number,
    default: 0
  },
  forumId: {
    type: Number,
    default: 0
  },
  order: {
    type: Number,
    default: CommentOrderV.TIME_ASC
  },
  disabled: {
    type: Boolean,
    default: false
  },
  replyId: {
    type: Number,
    default: undefined
  }
})
const { comment, authorId, order, disabled, forumId, replyId } = props
const { hasPermission } = useUserAuth(forumId)
const { replaceEmotions } = useEmotions()
const { openReportDialog } = useReport()
const { user } = useCurrentUser()

// 在组件挂载后，如果有replyId，滚动到对应的回复
onMounted(() => {
  if (replyId && comment.replies?.length) {
    nextTick(() => {
      const replyElement = document.querySelector('.reply-highlight')
      if (replyElement) {
        replyElement.scrollIntoView({ behavior: 'smooth', block: 'center' })
      }
    })
  }
})

function onReportReply(reply: CommentReplyVO) {
  if (!user.value.userId) {
    ElMessage.error('请登录后操作')
    return
  }
  openReportDialog(reply.replyId, 'REPLY', props.forumId)
}

const toReply = (reply: ExtendedCommentReplyVO, authorId: number) => {
  if (authorId === reply.authorId) {
    reply._replyAuthorId = 0
  } else {
    reply._replyAuthorId = reply.authorId
  }
  reply._replyAuthorName = reply.authorName
  reply._showEditor = true
}
const loadMoreReplies = (comment: ExtendedCommentVO) => {
  comment.loading = true
  const pageSize = 20
  if (comment.pageNo) {
    comment.pageNo = comment.pageNo + 1
  } else {
    comment.pageNo = 1
  }
  queryCommentReplies(comment.commentId, order, comment.pageNo, pageSize, replyId)
    .then((res) => {
      const data = res.data
      if (comment.pageNo == 1) {
        comment.replies = data.records as ExtendedCommentReplyVO[]
      } else {
        comment.replies?.push(...(data.records as ExtendedCommentReplyVO[]))
      }
    })
    .catch((err) => {
      ElMessage.error(err as string)
    })
    .finally(() => {
      comment.loading = false
    })
}
const refreshCommentReplies = (comment: ExtendedCommentVO) => {
  comment.pageNo = 1
  const pageSize = 20
  queryCommentReplies(comment.commentId, order, comment.pageNo, pageSize, replyId)
    .then((res) => {
      const data = res.data
      comment.replies = data.records as ExtendedCommentReplyVO[]
      comment.replyCount = data.total
    })
    .catch((err) => {
      ElMessage.error(err as string)
    })
}
const open = (url: string) => {
  window.open(url, '_blank')
}
const handleLike = async (reply: CommentReplyVO) => {
  const user = useUser()
  if (user.value.userId === 0) {
    ElMessage.warning('请先登录再点赞')
    return
  }
  if (disabled) return

  try {
    const targetId = reply.replyId
    const targetType = 2
    const res = await toggleLike({
      targetType,
      targetId
    })
    if (res.data > 0) {
      reply.liked = true
    } else {
      reply.liked = false
    }
    reply.likes = reply.likes + res.data
  } catch (err) {
    ElMessage.error(err as string)
  }
}
</script>
<style lang="scss" scoped>
.reply-highlight {
  background-color: rgba(64, 158, 255, 0.1);
  transition: background-color 0.5s ease;
  padding: 5px;
  border-radius: 4px;
}
</style>
