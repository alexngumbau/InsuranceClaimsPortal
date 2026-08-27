export function Sidebar() {
  return (
    <aside className="sidebar">
      <div className="brand">
        <div className="brand-mark">J</div>
        <div>
          <strong>Jubilee</strong>
          <span>Insurance Group</span>
        </div>
      </div>

      <nav className="navigation" aria-label="Main navigation">
        <a className="nav-link active" href="#claims"><span>▦</span>Claims overview</a>
        <a className="nav-link" href="#policies"><span>▤</span>Policies</a>
        <a className="nav-link" href="#customers"><span>◉</span>Customers</a>
      </nav>

      <div className="sidebar-footer">
        <span className="status-dot" />
        Claims operations online
      </div>
    </aside>
  )
}
