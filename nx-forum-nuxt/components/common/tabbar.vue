<template>
  <ClientOnly>
    <div class="sm:hidden relative w-full fixed-tabbar">
      <div class="h-[50px] w-full"></div>
      <van-tabbar v-model="active" @change="change">
        <van-tabbar-item name="home" icon="home-o">
          首页
        </van-tabbar-item>
        <van-tabbar-item name="all" icon="list-switching">
          版块
        </van-tabbar-item>
        <van-tabbar-item name="notification" icon="envelop-o" :badge="userNotification.totalCount || ''">
          消息
        </van-tabbar-item>
        <van-tabbar-item name="user" icon="user-o">
          用户
        </van-tabbar-item>
      </van-tabbar>
    </div>
  </ClientOnly>
</template>

<script lang="ts" setup>
const active = ref('home')
const userNotification = useUserNotificationCount()
const change = (name: string) => {
  if (name === 'home') {
    navigateTo('/')
  } else if (name === 'all') {
    navigateTo('/all')
  } else if (name === 'notification') {
    navigateTo('/notification')
  } else if (name === 'user') {
    navigateTo('/user/mobile')
  }
}
onMounted(() => {
  if (window.location.pathname.indexOf('/all') > -1) {
    active.value = 'all'
  } else if (window.location.pathname.indexOf('/notification') > -1) {
    active.value = 'notification'
  } else if (window.location.pathname.indexOf('/user') > -1) {
    active.value = 'user'
  } else {
    active.value = 'home'
  }
})
</script>
