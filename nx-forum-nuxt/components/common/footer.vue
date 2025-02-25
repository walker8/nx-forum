<template>
  <div
    class="hidden sm:flex mt-2 flex-col items-center justify-center h-full text-[#626675] text-[13px] leading-[21px] pb-[10px]"
  >
    <div class="flex space-x-2">
      <div @click="go('/about')" class="cursor-pointer">关于我们</div>
      <div class="text-gray-300">|</div>
      <div @click="go('/agreement')" class="cursor-pointer">用户协议</div>
      <div class="text-gray-300">|</div>
      <div @click="go('/privacy')" class="cursor-pointer">隐私政策</div>
    </div>
    <div>
      2025 © Powered by
      <a href="https://github.com/walker8/nx-forum" target="_blank"><b>nx-forum</b></a>
    </div>
  </div>
</template>
<script setup lang="ts">
const go = (path: string) => {
  window.open(path, '_blank')
}
</script>
