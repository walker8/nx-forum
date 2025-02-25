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
          <el-popover placement="bottom" :width="400" trigger="click" :visible="popVisible">
            <template #reference>
              <el-button type="primary" @click="togglePop" :disabled="!menus?.length">
                {{ threadCmd.threadId && threadCmd.threadId > 0 ? '更新' : '发布' }}
              </el-button>
            </template>
            <editor-post @cancel="cancel" />
          </el-popover>
        </el-space>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
const threadCmd = useThreadCmd()
const popVisible = ref(false)
const menus = useUserMenus()
const cancel = () => {
  popVisible.value = false
}
const togglePop = () => {
  popVisible.value = !popVisible.value
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
</style>
