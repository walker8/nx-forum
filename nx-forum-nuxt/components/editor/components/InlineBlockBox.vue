<script lang="ts" setup>
import { NodeViewWrapper } from "@tiptap/vue-3";
</script>
<template>
  <NodeViewWrapper as="div" class="inline-block-box inline-block">
    <slot></slot>
  </NodeViewWrapper>
</template>
<style scoped lang="scss">
.inline-block-box {
  width: calc(100% - 1px);
  max-width: 100%;
}
</style>
