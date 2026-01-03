<template>
  <el-container>
    <el-header style="padding: 0px" class="rn-hide">
      <common-header />
    </el-header>
    <div class="common-layout">
      <el-container>
        <el-aside width="200px" class="hidden-md-and-down">
          <div class="relative z-1000" ref="navbarRef">
            <div
              :class="{ 'fixed mt-5 top-0': isSticky }"
              class="w-[200px] overflow-y-auto bg-white shadow-sm"
            >
              <div class="flex flex-col w-full">
                <div
                  v-for="menu in userSettings.settings"
                  :key="menu.value"
                  :class="[
                    'flex items-center px-5 py-3.5 cursor-pointer transition-colors duration-300 text-gray-500 hover:bg-gray-50',
                    { 'text-gray-900 bg-gray-50': userSettings.selected === menu.value }
                  ]"
                  @click="goUserSettings(menu.value)"
                >
                  <span class="text-sm">{{ menu.label }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-aside>
        <el-main class="forum-main">
          <div class="hidden-lg-and-up m-header">
            <van-tabs v-model:active="userSettings.selected" sticky shrink @click-tab="onClickTab">
              <van-tab
                v-for="menu in userSettings.settings"
                :title="menu.label"
                :name="menu.value"
              />
            </van-tabs>
          </div>
          <slot />
        </el-main>
      </el-container>
    </div>
  </el-container>
</template>
<script setup lang="ts">
const route = useRoute()
const userSettings = useUserSettings()
if (route.path.endsWith('account')) {
  userSettings.value.selected = 'account'
} else {
  userSettings.value.selected = ''
}
const onClickTab = ({ name }: { name: string }) => {
  goUserSettings(name)
}
const goUserSettings = (name: string) => {
  userSettings.value.selected = name
  navigateTo(`/uc/settings/${name}`)
}

// 添加滚动固定功能
const navbarRef = ref<HTMLElement | null>(null)
const isSticky = ref(false)

const handleScroll = () => {
  if (navbarRef.value) {
    const rect = navbarRef.value.getBoundingClientRect()
    isSticky.value = rect.top <= 0
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>
<style lang="scss" scoped>
.common-layout {
  height: 100%;
  width: fit-content;
  margin: 0 auto;
  margin-top: 10px;
}
.m-header {
  width: 100%;
  margin-bottom: 5px;
}
.m-header .el-card {
  --el-card-padding: 10px;
}
.forum-main {
  padding-top: 0px;
  max-width: 720px;
}
@media screen and (max-width: 720px) {
  .common-layout {
    width: 100%;
  }
}
@media screen and (max-width: 960px) {
  .el-main {
    padding: 0px;
  }
  .forum-main {
    width: calc(100vw - 40px);
  }
}
@media screen and (min-width: 960px) {
  .forum-main {
    width: 720px;
  }
}
</style>
