<template>
  <el-container class="my-container">
    <div class="content">
      <el-header style="padding: 0px">
        <common-header />
      </el-header>
      <div class="common-layout">
        <el-container>
          <el-main class="my-main">
            <slot />
          </el-main>
        </el-container>
      </div>
    </div>
  </el-container>
</template>
<style lang="scss" scoped>
.common-layout {
  height: 100%;
  width: fit-content;
  margin: 0 auto;
  margin-top: 10px;
}
.my-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh; /* 让容器至少占据整个视口高度 */
}
.content {
  flex-grow: 1; /* 让主要内容区域尽可能占据剩余空间 */
}
.my-main {
  padding-top: 0px;
  max-width: 720px;
}
@media screen and (max-width: 768px) {
  .common-layout {
    width: 100%;
  }
}
@media screen and (max-width: 960px) {
  .el-main {
    padding: 0px;
  }
  .my-main {
    width: calc(100vw - 40px);
  }
}
@media screen and (min-width: 960px) {
  .my-main {
    width: 720px;
  }
}
</style>
