import type { Thread, ThreadCmd } from '~/types/global'

export const useThread = () =>
  useState<Thread>('thread', () => ({
    threadId: 0,
    forumId: 0,
    categoryId: 0,
    content: '',
    comments: 0,
    subject: '',
    top: false,
    blocked: false,
    closed: false,
    digest: false,
    author: null,
    createTime: '',
    updateAuthor: null,
    updateTime: '',
    docType: 0,
    views: 0,
    likes: 0,
    dislikes: 0,
    collections: 0,
    auditStatus: 0,
    commentOrder: 0,
    imageCount: 0,
    seoTitle: '',
    seoContent: '',
    forumName: '',
    forumNickName: '',
    errMsg: '',
    liked: false,
    collected: false
  }))

export const useThreadCmd = () =>
  useState<ThreadCmd>('threadCmd', () => ({
    threadId: 0,
    forumId: 0,
    categoryId: 0,
    commentOrder: 0,
    content: '',
    subject: ''
  }))
