import type { ClaimMetrics as ClaimMetricsData } from '../api/claimsApi'

interface ClaimMetricsProps { metrics: ClaimMetricsData | null }

export function ClaimMetrics({ metrics }: ClaimMetricsProps) {
  const approvedAmount = metrics?.approvedAmount.toLocaleString('en-KE') ?? '—'
  return (
    <section className="metrics" aria-label="Claims summary">
      <article className="metric-card"><span className="metric-label">Total claims</span><strong>{metrics?.totalClaims ?? '—'}</strong><small>From all recorded claims</small></article>
      <article className="metric-card accent"><span className="metric-label">Pending review</span><strong>{metrics?.pendingReview ?? '—'}</strong><small>Submitted or under review</small></article>
      <article className="metric-card"><span className="metric-label">Approved amount</span><strong>{metrics ? `KES ${approvedAmount}` : '—'}</strong><small>Approved and paid claims</small></article>
      <article className="metric-card"><span className="metric-label">Paid claims</span><strong>{metrics?.paidClaims ?? '—'}</strong><small>From all recorded claims</small></article>
    </section>
  )
}
