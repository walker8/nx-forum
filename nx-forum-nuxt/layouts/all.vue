<template>
  <el-container class="forum-container">
    <div class="content">
      <el-affix>
        <common-header type="search" />
      </el-affix>
      <div class="common-layout mx-auto sm:mt-2">
        <el-container>
          <el-aside width="200px" class="hidden-md-and-down" v-if="forumMenu.menus?.length > 0">
            <ForumLeft />
          </el-aside>
          <el-main class="forum-main">
            <slot />
          </el-main>
          <el-aside class="hidden-sm-and-down">
            <div class="w-67.5"></div>
          </el-aside>
        </el-container>
      </div>
    </div>
    <CommonFooter />
    <CommonTabbar />
  </el-container>
</template>
<script setup lang="ts">
import { getForumShowMenu } from '~/apis/forum'
const forumPostPage = useForumPostPage()
const forumMenu = useForumMenu()
forumMenu.value.selctedMenu = 'all'
if (forumMenu.value.menus?.length <= 0) {
  try {
    let res = await getForumShowMenu()
    const data = res.data
    forumMenu.value.menus = data.records
  } catch (e) {
    forumPostPage.value.errMsg = String(e)
  }
}
</script>
