<template>
  <el-container>
    <el-header style="padding: 0px" class="rn-hide">
      <common-header />
    </el-header>
    <div class="common-layout">
      <div class="error-page" v-if="errorMsg">
        <el-empty :description="errorMsg" />
      </div>
      <div class="success-page" v-else>
        <el-container>
          <el-aside width="200px" class="hidden-md-and-down">
            <el-menu :default-active="userNotification.selected" class="el-menu-vertical-demo">
              <el-menu-item
                v-for="menu in userNotification.tabs"
                :index="menu.value"
                @click="goNotificationTabs(menu.value)"
              >
                <span>{{ menu.label }}</span>
                <el-tag
                  type="danger"
                  effect="dark"
                  size="small"
                  class="ml-2"
                  v-if="userNotificationCount[menu.count] > 0"
                >
                  {{ userNotificationCount[menu.count] }}
                </el-tag>
              </el-menu-item>
            </el-menu>
          </el-aside>
          <el-main class="forum-main">
            <div class="hidden-lg-and-up m-header">
              <van-tabs
                v-model:active="userNotification.selected"
                sticky
                shrink
                @click-tab="onClickTab"
              >
                <van-tab
                  v-for="menu in userNotification.tabs"
                  :title="menu.label"
                  :name="menu.value"
                  :dot="userNotificationCount[menu.count] > 0"
                />
              </van-tabs>
            </div>
            <slot />
          </el-main>
        </el-container>
      </div>
    </div>
    <CommonTabbar />
  </el-container>
</template>
<script setup lang="js">
const userNotification = useUserNotification()
const userNotificationCount = useUserNotificationCount()
const errorMsg = ref('')
const onClickTab = ({ name }) => {
  goNotificationTabs(name)
}
const goNotificationTabs = (name) => {
  if ('reply' === name) {
    navigateTo(`/notification`)
  } else {
    navigateTo(`/notification/${name}`)
  }
}
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
.user-header {
  margin-right: 20px;
  margin-bottom: 20px;
}
.user-name {
  font-size: 24px;
  color: black;
}
.user-name,
.user-intro,
.user-time {
  display: block;
}
.user-time {
  margin-top: 5px;
}
.forum-main {
  padding-top: 0px;
  max-width: 720px;
}
@media screen and (max-width: 1200px) {
  .user-header {
    margin-left: 0px;
    max-width: 720px;
  }
  .el-main {
    --el-main-padding: 0px;
  }
}
@media screen and (max-width: 960px) {
  .el-main {
    padding: 0px;
  }
  .forum-main {
    width: calc(100vw - 40px);
  }
  .user-header {
    margin-left: 0px;
    margin-right: 0px;
  }
}
@media screen and (max-width: 768px) {
  .common-layout {
    width: 100%;
  }
}
@media screen and (min-width: 960px) {
  .forum-main {
    width: 720px;
  }
}
</style>
