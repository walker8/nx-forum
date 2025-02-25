export enum AuditStatus {
  PASSED,
  AUDITING,
  REJECTED
}
export const useAuditStatus = (status: string) => {
  const route = useRoute()
  let auditStatus: AuditStatus | null = null
  let deleted = false
  switch (status) {
    case 'recycle':
      deleted = true
      break
    case 'auditing':
      auditStatus = AuditStatus.AUDITING
      break
    default:
      auditStatus = AuditStatus.PASSED
      break
  }
  return { status, auditStatus, deleted }
}

export const useAuditingCount = () =>
  useState('auditingCount', () => ({
    threadAuditCount: 0,
    commentAuditCount: 0,
    replyAuditCount: 0,
    totalAuditCount: 0,
    pendingReportCount: 0
  }))
