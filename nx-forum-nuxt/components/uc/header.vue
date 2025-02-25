<template>
  <div class="header">
    <div class="main">
      <div class="left">
        <el-icon class="toggle-sidebar-btn" @click="$emit('toggleSidebar')">
          <Expand v-if="isCollapse" />
          <Fold v-else />
        </el-icon>
      </div>
      <div class="right">
        <el-space wrap alignment="center" :size="10">
          <el-dropdown>
            <div class="el-dropdown-avatar">
              <el-avatar :size="32" :src="user.avatar || '/img/avatar.png'" />
              <span class="el-dropdown-link">
                {{ user.userName }}
                <el-icon class="el-icon--right">
                  <ArrowDown />
                </el-icon>
              </span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-space>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ArrowDown, Expand, Fold } from '@element-plus/icons-vue'

defineProps<{
  isCollapse: boolean
}>()

defineEmits(['toggleSidebar'])

const { user, handleLogout } = useCurrentUser()
</script>

<style lang="scss" scoped>
.header {
  width: 100%;
  height: 60px;
  background-color: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-light);
}

.main {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 15px;
}

.left {
  .toggle-sidebar-btn {
    cursor: pointer;
    font-size: 20px;
    margin-right: 16px;
    color: rgb(138, 145, 159);

    &:hover {
      color: var(--el-color-primary);
    }
  }
}

.right {
  display: flex;
  align-items: center;
}

.el-dropdown-avatar {
  display: flex;
  align-items: center;
  cursor: pointer;
  .el-dropdown-link {
    margin-left: 8px;
    color: var(--el-text-color-primary);
  }
}
</style>
