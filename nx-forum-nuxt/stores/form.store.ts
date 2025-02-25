import { defineStore } from 'pinia'
export const useAricleFormSearchStore = defineStore(
  'article-form-search',
  () => {
    return {
      title: '',
      host: '',
      url: '',
      articleType: '',
      isDeleted: false,
      currentPage: 1,
      pageSize: 20
    }
  },
  {
    persist: {
      storage: persistedState.localStorage
    }
  }
)

export const usePlanRecordFormSearchStore = defineStore(
  'plan-record-form-search',
  () => {
    return {
      title: '',
      planStatus: -1,
      source: -1,
      url: '',
      currentPage: 1,
      pageSize: 50
    }
  },
  {
    persist: {
      storage: persistedState.localStorage
    }
  }
)

export const useSpiderRecordFormSearchStore = defineStore(
  'spider-record-form-search',
  () => {
    return {
      recordType: -1,
      recordStatus: -1,
      currentPage: 1,
      pageSize: 50
    }
  },
  {
    persist: {
      storage: persistedState.localStorage
    }
  }
)
