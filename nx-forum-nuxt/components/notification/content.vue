<template>
  <el-card>
    <el-empty :description="notificationPage.errMsg" v-if="notificationPage.errMsg" />
    <div v-else style="margin-top: -12px">
      <el-empty description="暂无内容" v-if="notificationPage.records?.length === 0" />
      <div v-for="notification in notificationPage.records">
        <div
          style="padding-top: 12px"
          :class="{ 'text-gray-500': notification.notificationStatus == 1 }"
        >
          <div
            v-if="notification.url"
            class="hover:cursor-pointer hover:text-[#409eff]"
            v-html="notification.message"
            @click="open(notification.url)"
          />
          <div v-else v-html="notification.message" />
          <div class="bg-gray-100 p-2 mt-1 text-xs" v-if="notification.postId">
            <span>来自主题：</span>
            <a
              :href="`/t/${notification.postId}`"
              target="_blank"
              class="cusor-pointer hover:text-[#409eff]"
            >
              {{ notification.postTitle }}
            </a>
          </div>
          <div class="flex text-sm pt-2 gap-1 items-end">
            <a
              v-if="notification.authorId"
              class="cusor-point hover:text-[#409eff]"
              :href="`/user/${notification.authorId}`"
              target="_blank"
            >
              {{ notification.authorName }}
            </a>
            <div class="text-gray-500 text-xs">{{ notification.createTime }}</div>
          </div>
        </div>
        <el-divider style="margin: 0px; margin-top: 8px" />
      </div>
      <common-load-more
        :has-next="notificationPage.hasNext"
        :disable-load-more="disableLoadMore"
        @load-more="loadMoreNotifications"
        class="mr-4 ml-4 pt-1"
      >
        加载更多内容
      </common-load-more>
    </div>
  </el-card>
</template>
<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { clearNotifications, queryNotifications } from '~/apis/notification'
import type { CustomPage, NotificationVO } from '~/types/global'
const props = defineProps({
  notificationType: {
    type: String,
    default: 'reply'
  }
})
const userNotification = useUserNotificationCount()
const { notificationType } = props
const notificationPage: CustomPage<NotificationVO> = reactive({
  current: 1,
  errMsg: '',
  hasNext: false,
  records: [],
  size: 20,
  total: 0
})
const pageSize = 20
const disableLoadMore = ref(false)
queryNotifications(notificationType, notificationPage.current, pageSize)
  .then((res) => {
    Object.assign(notificationPage, res.data)
    // 消息不为空则清空消息
    clearNotifications(notificationType)
      .then(() => {
        clearNotificationCount(notificationType)
      })
      .catch((err) => {
        ElMessage.error(err)
      })
  })
  .catch((err) => {
    notificationPage.errMsg = err
  })
const getNotificationCount = (notificationType: string) => {
  if (notificationType == 'at') {
    return userNotification.value.mentionCount
  } else if (notificationType == 'system') {
    return userNotification.value.systemCount
  } else {
    return userNotification.value.replyCount
  }
}

const clearNotificationCount = (notificationType: string) => {
  if (notificationType == 'at') {
    userNotification.value.mentionCount = 0
  } else if (notificationType == 'system') {
    userNotification.value.systemCount = 0
  } else {
    userNotification.value.replyCount = 0
  }
  userNotification.value.totalCount =
    userNotification.value.mentionCount +
    userNotification.value.systemCount +
    userNotification.value.replyCount
}

const loadMoreNotifications = () => {
  const pageNo = notificationPage.current + 1
  queryNotifications(notificationType, pageNo, pageSize)
    .then((res) => {
      const data = res.data
      notificationPage.records.push(...data.records)
      notificationPage.hasNext = data.hasNext
      notificationPage.total = data.total
      notificationPage.current = data.current
      notificationPage.size = data.size
    })
    .catch((err) => {
      ElMessage.error(err)
    })
    .finally(() => {
      disableLoadMore.value = false
    })
}

const open = (url: string) => {
  if (url) {
    window.open(url, '_blank')
  }
}
</script>
