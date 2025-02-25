<template>
  <div class="mobile-user-page">
    <van-cell-group class="user-info" :border="false">
      <van-cell center :border="false">
        <template #title>
          <div class="user-profile">
            <van-image round width="60" height="60" :src="user.avatar || '/img/avatar.png'" />
            <div>
              <div class="user-name" @click="goUserHome(user.userId)">
                {{ user.userName || '未登录' }}
              </div>
              <div v-if="user.intro">{{ user.intro }}</div>
            </div>
          </div>
        </template>
      </van-cell>
    </van-cell-group>

    <van-cell-group class="menu-group">
      <van-cell title="我的主页" is-link @click="goUserHome(user.userId)">
        <template #icon>
          <van-icon name="home-o" class="menu-icon" />
        </template>
      </van-cell>

      <van-cell title="个人设置" is-link to="/uc/settings/profile">
        <template #icon>
          <van-icon name="setting-o" class="menu-icon" />
        </template>
      </van-cell>

      <ClientOnly>
        <van-cell title="后台管理" is-link to="/admin" v-if="hasPermission('admin:manage')">
          <template #icon>
            <van-icon name="manager-o" class="menu-icon" />
          </template>
        </van-cell>
      </ClientOnly>
    </van-cell-group>

    <van-cell-group class="menu-group" v-if="user.userId">
      <van-cell title="退出登录" @click="handleLogout" :border="false">
        <template #icon>
          <van-icon name="clear" class="menu-icon" />
        </template>
      </van-cell>
    </van-cell-group>

    <van-cell-group class="menu-group" v-else>
      <van-cell title="立即登录" is-link to="/uc/login" :border="false">
        <template #icon>
          <van-icon name="user-o" class="menu-icon" />
        </template>
      </van-cell>
    </van-cell-group>
    <common-tabbar />
  </div>
</template>

<script setup lang="ts">
const { user, handleLogout } = useCurrentUser()
const { hasPermission } = useUserAuth()
const goUserHome = (userId: number) => {
  if (userId && userId > 0) {
    window.open(`/user/${userId}`, '_blank')
  } else {
    navigateTo('/uc/login')
  }
}
</script>

<style lang="scss" scoped>
.mobile-user-page {
  background-color: #f7f8fa;
  padding-bottom: 20px;

  .user-info {
    padding: 20px 0;
    background-color: white;
    margin-bottom: 12px;
  }

  .user-profile {
    display: flex;
    align-items: center;
    gap: 15px;

    :deep(.van-image) {
      flex-shrink: 0;
      min-width: 60px;
      min-height: 60px;
    }

    .user-name {
      font-size: 18px;
      font-weight: 500;
    }

    div:last-child {
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      color: #666;
      font-size: 14px;
    }
  }

  .menu-group {
    margin-bottom: 12px;

    .menu-icon {
      font-size: 20px;
      margin-right: 8px;
    }
  }

  :deep(.van-cell) {
    align-items: center;
    padding: 15px 16px;
  }
}
</style>
