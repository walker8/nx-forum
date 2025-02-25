import { defineStore } from 'pinia'
export const useGlobalStore = defineStore(
  'global',
  () => {
    return {
      httpOnly: process.env.NODE_ENV !== 'development' && false
    }
  },
  {
    persist: import.meta.client && {
      storage: persistedState.cookiesWithOptions({
        sameSite: 'strict'
      })
    }
  }
)
