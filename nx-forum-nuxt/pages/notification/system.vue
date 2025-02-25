<template>
  <notification-content notification-type="system" />
</template>
<script setup lang="ts">
definePageMeta({
  layout: 'notification'
})
useSeoMeta({
  title: '消息通知'
})
const userNotification = useUserNotification()
userNotification.value.selected = 'system'
</script>
