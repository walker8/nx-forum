<template>
  <div class="search-layout">
    <el-container>
      <el-affix>
        <common-header type="search" />
      </el-affix>
      <div class="min-h-[calc(100vh-120px)] p-2.5 px-0 w-full md:w-[720px] mx-auto">
        <slot />
      </div>
      <el-footer><common-footer /></el-footer>
    </el-container>
  </div>
</template>
