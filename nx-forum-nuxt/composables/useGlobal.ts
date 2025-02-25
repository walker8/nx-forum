// 页面回到顶部（滚动效果）
export const handleScroll = () => {
  if (import.meta.client) {
    let scrollTop = window.scrollY || document.documentElement.scrollTop || document.body.scrollTop
    if (scrollTop > 0) {
      const timeTop = setInterval(() => {
        document.documentElement.scrollTop = document.body.scrollTop = scrollTop -= 50 //一次减50往上滑动
        if (scrollTop <= 0) {
          clearInterval(timeTop)
        }
      }, 10) //定时调用函数使其更顺滑
    }
  }
}
