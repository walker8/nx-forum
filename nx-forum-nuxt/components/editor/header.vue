<template>
  <div class="header">
    <div class="main">
      <div class="left">
        <div class="cursor-pointer" @click="goHome">
          <CommonLogo />
        </div>
      </div>
      <div class="right">
        <el-space wrap>
          <client-only>
            <el-button type="primary" @click="openDialog" :disabled="!menus?.length">
              {{ threadCmd.threadId && threadCmd.threadId > 0 ? '更新' : '发布' }}
            </el-button>

            <el-dialog
              v-model="dialogVisible"
              :title="threadCmd.threadId && threadCmd.threadId > 0 ? '更新帖子' : '发布帖子'"
              :width="isMobile ? '100%' : '500px'"
              :fullscreen="isMobile"
              :class="{ 'mobile-dialog': isMobile }"
              :top="isMobile ? '0' : '15vh'"
              :destroy-on-close="true"
              append-to-body
            >
              <editor-post @cancel="closeDialog" />
            </el-dialog>
          </client-only>
        </el-space>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { getUserForumMenu } from '~/apis/forum'

const threadCmd = useThreadCmd()
const dialogVisible = ref(false)
const isMobile = ref(false)
const menus = useUserMenus()
const isClient = import.meta.client

const openDialog = () => {
  dialogVisible.value = true
}

const closeDialog = () => {
  dialogVisible.value = false
}

onMounted(() => {
  if (isClient) {
    checkScreenSize()
    window.addEventListener('resize', checkScreenSize)
    // Fetch user forum menus to enable the publish button
    if (!menus?.value || menus.value.length === 0) {
      getUserForumMenu().then((res) => {
        menus.value = res.data
      })
    }
  }
})

onUnmounted(() => {
  if (isClient) {
    window.removeEventListener('resize', checkScreenSize)
  }
})

function checkScreenSize() {
  if (isClient) {
    isMobile.value = window.innerWidth < 768
  }
}

const goHome = () => {
  window.open('/', '_self')
}
</script>
<style scoped>
.el-dropdown-avatar {
  cursor: pointer;
  color: var(--el-color-primary);
  display: flex;
  align-items: center;
}
.header {
  height: 55px;
  background-color: white;
}
.main {
  display: flex;
  align-items: center;
  height: 55px;
  margin: 0 auto;
  max-width: 1200px;
  padding-left: 10px;
  padding-right: 10px;
}
.right {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  width: 100%;
}

.mobile-dialog :deep(.el-dialog__header) {
  display: none;
}

.mobile-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

@media (max-width: 767px) {
  .mobile-dialog :deep(.el-dialog__body) {
    padding-bottom: 80px; /* Space for fixed footer buttons */
  }
}
</style>
