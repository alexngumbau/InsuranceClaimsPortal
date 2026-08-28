interface DashboardHeaderProps {
  onCreateClaim: () => void
}

export function DashboardHeader({ onCreateClaim }: DashboardHeaderProps) {
  const today = new Date().toLocaleDateString('en-GB', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  })

  return (
    <div className="page-heading">
      <div>
        <p className="eyebrow">{today}</p>
        <h2>Claims overview</h2>
        <p className="muted">Monitor and manage customer claims across Jubilee.</p>
      </div>
      <button className="primary-button" type="button" onClick={onCreateClaim}>
        <span>+</span> Create claim
      </button>
    </div>
  )
}
