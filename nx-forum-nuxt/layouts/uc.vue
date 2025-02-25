<template>
  <el-container>
    <!-- 移动端遮罩层 -->
    <div v-if="isMobile && !isCollapse" class="sidebar-overlay" @click="isCollapse = true"></div>

    <!-- 侧边栏 -->
    <el-aside
      class="sidebar"
      :class="{ 'mobile-sidebar': isMobile }"
      :width="isCollapse ? '64px' : '200px'"
      :style="isMobile && isCollapse ? 'width: 0 !important; min-width: 0;' : ''"
    >
      <UcSidebar :collapse="isCollapse" @close-sidebar="isCollapse = true" />
    </el-aside>

    <el-container>
      <el-header style="padding: 0px">
        <UcHeader @toggle-sidebar="toggleSidebar" :is-collapse="isCollapse" />
      </el-header>
      <el-main class="uc-main">
        <slot />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
const isCollapse = ref(false)
const isMobile = ref(false)

// 检测设备是否为移动端
const checkIsMobile = () => {
  isMobile.value = window.innerWidth < 768
  // 在移动端默认收起侧边栏
  if (isMobile.value) {
    isCollapse.value = true
  }
}

// 切换侧边栏状态
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// 监听窗口大小变化
onMounted(() => {
  checkIsMobile()
  window.addEventListener('resize', checkIsMobile)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', checkIsMobile)
})

useSeoMeta({
  title: '用户中心管理 - nx-forum'
})
</script>

<style lang="scss" scoped>
.el-container {
  height: 100vh;
  display: flex;
  position: relative; /* 为遮罩层定位 */
}

.sidebar {
  background-color: var(--v3-sidebar-menu-bg-color);
  transition:
    width 0.3s,
    transform 0.3s;
  z-index: 1000;
}

/* 移动端侧边栏样式 */
.mobile-sidebar {
  position: fixed;
  height: 100vh;
  left: 0;
  top: 0;
  transform: translateX(0);

  &:not(.is-collapse) {
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
  }
}

/* 侧边栏遮罩层 */
.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 999;
}

.uc-main {
  padding: 20px;
  background-color: var(--v3-body-bg-color);
}

/* 移动端适配 */
@media (max-width: 768px) {
  .uc-main {
    padding: 10px;
  }
}
</style>
