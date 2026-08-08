<template>
  <div class="relative z-1000" ref="navbarRef">
    <div
      :class="{ 'fixed mt-5 top-0': isSticky }"
      class="forum-left-nav w-[200px] overflow-y-auto bg-white rounded-lg border border-[#e4e6eb]"
    >
      <div class="flex flex-col w-full py-2">
        <div
          v-for="menu in forumMenu.menus"
          :key="menu.name"
          :class="[
            'nav-item flex items-center mx-2 px-3 py-2.5 rounded-md cursor-pointer transition-colors duration-200 text-[#515767] hover:bg-[#f2f3f5]',
            { 'nav-active': forumMenu.selctedMenu === menu.name }
          ]"
          @click="goForum(menu.name)"
        >
          <div class="flex items-center justify-center mr-2.5">
            <Icon :name="menu.iconName" class="w-5 h-5" />
          </div>
          <span class="text-sm truncate">{{ menu.nickName }}</span>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
const forumMenu = useForumMenu()
const goForum = (path) => {
  if (path === 'forums') {
    navigateTo('/forums')
  } else {
    navigateTo(`/f/${path}`)
  }
}

const navbarRef = ref(null)
const isSticky = ref(false)

const handleScroll = () => {
  if (navbarRef.value) {
    const rect = navbarRef.value.getBoundingClientRect()
    isSticky.value = rect.top <= 0
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style lang="scss" scoped>
.nav-item {
  & + .nav-item {
    margin-top: 2px;
  }
}

.nav-active {
  color: #1e80ff;
  font-weight: 500;
  background-color: rgba(30, 128, 255, 0.08);

  &:hover {
    background-color: rgba(30, 128, 255, 0.12);
  }
}
</style>
