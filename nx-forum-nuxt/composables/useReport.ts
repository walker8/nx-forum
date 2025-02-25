export const useReport = () => {
  const reportDialogVisible = useState('reportDialogVisible', () => false)
  const reportData = useState(
    'reportData',
    () =>
      ({
        targetId: 0 as number,
        targetType: 'THREAD' as 'THREAD' | 'COMMENT' | 'REPLY',
        forumId: 0 as number,
      }),
  )

  function openReportDialog(
    targetId: number,
    targetType: 'THREAD' | 'COMMENT' | 'REPLY',
    forumId: number,
  ) {
    reportData.value.targetId = targetId
    reportData.value.targetType = targetType
    reportData.value.forumId = forumId
    reportDialogVisible.value = true
  }

  return {
    reportDialogVisible,
    reportData,
    openReportDialog,
  }
} 