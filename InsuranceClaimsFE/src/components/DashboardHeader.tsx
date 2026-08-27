interface DashboardHeaderProps {
  onCreateClaim: () => void
}

export function DashboardHeader({ onCreateClaim }: DashboardHeaderProps) {
  return (
    <>
      <header className="topbar">
        <div>
          <p className="eyebrow">Claims operations</p>
          <h1>Good morning, Alex</h1>
        </div>
        <div className="profile">
          <div className="avatar">AO</div>
          <div>
            <strong>Alex Otieno</strong>
            <span>Claims officer</span>
          </div>
        </div>
      </header>

      <div className="page-heading">
        <div>
          <p className="eyebrow">Thursday, 27 August 2026</p>
          <h2>Claims overview</h2>
          <p className="muted">Monitor and manage customer claims across Jubilee.</p>
        </div>
        <button className="primary-button" type="button" onClick={onCreateClaim}>
          <span>+</span> Create claim
        </button>
      </div>
    </>
  )
}
