<template>
  <el-container>
    <el-header style="padding: 0px">
      <editor-header />
    </el-header>
    <el-container>
      <el-main>
        <slot />
      </el-main>
    </el-container>
  </el-container>
</template>
<script setup></script>
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
@media screen and (max-width: 768px) {
  .common-layout {
    width: 100%;
  }
}
.el-main {
  padding: 0px;
  overflow: hidden;
}
</style>
