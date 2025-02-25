<template>
  <div class="relative z-1000" ref="navbarRef">
    <div
      :class="{ 'fixed mt-5 top-0': isSticky }"
      class="w-[200px] overflow-y-auto bg-white shadow-sm"
    >
      <div class="flex flex-col w-full">
        <div
          v-for="menu in forumMenu.menus"
          :key="menu.name"
          :class="[
            'flex items-center px-5 py-3.5 cursor-pointer transition-colors duration-300 text-gray-500 hover:bg-gray-50',
            { 'text-gray-900 bg-gray-50': forumMenu.selctedMenu === menu.name }
          ]"
          @click="goForum(menu.name)"
        >
          <div class="flex items-center justify-center mr-3">
            <Icon :name="menu.iconName" class="w-5 h-5" />
          </div>
          <span class="text-sm">{{ menu.nickName }}</span>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
const forumMenu = useForumMenu()
const goForum = (path) => {
  if (path === 'all') {
    navigateTo('/all')
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
