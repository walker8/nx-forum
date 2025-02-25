import type { CustomPage, ForumInfo, Thread, ForumMenuItemVO } from '~/types/global'

export const useForumPostPage = () =>
  useState<CustomPage<Thread>>('forumPostPage', () => ({
    total: 0,
    size: 0,
    current: 0,
    loading: false,
    hasNext: false,
    errMsg: '',
    records: []
  }))

export const useForumInfo = () =>
  useState<ForumInfo>('forumInfo', () => ({
    forumId: 0,
    name: '',
    nickName: '',
    threadCount: 0,
    followCount: 0,
    shortBrief: '',
    brief: '',
    iconName: '',
    seoTitle: '',
    seoKeywords: '',
    seoContent: '',
    backgroundImage: '',
    isCurrentUserAdmin: false,
    followed: false
  }))

export const useForumMenu = () =>
  useState('forumMenu', () => ({
    selctedMenu: '',
    menus: [] as ForumMenuItemVO[]
  }))
