<template>
  <el-container>
    <el-header style="padding: 0" class="hidden sm:block">
      <common-header />
    </el-header>
    <el-card v-if="thread.errMsg" class="thread-main" style="margin: 0 auto">
      <el-empty :description="thread.errMsg" />
      <div class="w-full flex justify-center pb-10">
        <el-button type="primary" @click="goHome">回到首页</el-button>
      </div>
    </el-card>
    <div v-else class="common-layout" ref="commonLayout">
      <el-container>
        <el-aside width="70px" class="hidden md:block">
          <threadLeftNav />
        </el-aside>
        <el-main class="thread-main">
          <slot />
        </el-main>
        <el-aside width="270px" class="hidden-md-and-down right-aside">
          <thread-author :author="thread.author" />
          <client-only>
            <thread-catalog :content="thread.content" />
          </client-only>
        </el-aside>
      </el-container>
    </div>
    <!-- 移动端底部导航 -->
    <thread-mobile-nav v-if="!thread.errMsg"/>
  </el-container>
</template>

<script setup lang="ts">
import { getThreadForView } from '@/apis/thread'
const thread = useThread()
const route = useRoute()
const threadId = Number(route.params.id)

try {
  if (threadId) {
    const res = await getThreadForView(threadId)
    // 获取文章成功
    const data = res.data
    thread.value = data
    thread.value.errMsg = ''
  }
} catch (errMsg) {
  // 获取文章失败
  thread.value.errMsg = String(errMsg)
}

const goHome = () => {
  navigateTo({
    path: '/'
  })
}
</script>

<style lang="scss" scoped>
.common-layout {
  height: 100%;
  width: fit-content;
  margin: 0 auto;
  margin-top: 10px;
}

.thread-main {
  padding-top: 0px;
  max-width: 800px;
}

.el-main {
  padding-left: 5px;
}

.el-card {
  margin-bottom: 15px;
}

@media screen and (max-width: 768px) {
  .common-layout {
    width: 100%;
  }
  .thread-main {
    width: 100% !important;
  }
}

@media screen and (max-width: 960px) {
  .el-main {
    padding: 0px;
  }

  .thread-main {
    width: calc(100vw - 100px);
  }
}

@media screen and (min-width: 960px) {
  .thread-main {
    width: 800px;
  }
}

.right-aside {
  position: relative;
}

@media screen and (min-width: 960px) {
  .right-aside {
    width: 270px;
    margin-left: 20px;
  }
}
</style>
