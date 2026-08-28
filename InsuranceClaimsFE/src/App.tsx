import './App.css'
import { lazy, Suspense, useEffect, useState } from 'react'
import { ConfigProvider, message } from 'antd'
import { createClaim, getClaimMetrics, listClaims } from './api/claimsApi'
import { ClaimMetrics } from './components/ClaimMetrics'
import { ClaimDetailsDrawer, ClaimsTable } from './components/ClaimsTable'
import { DashboardHeader } from './components/DashboardHeader'
import { Sidebar } from './components/Sidebar'
import { useClaimFilters } from './hooks/useClaimFilters'
import type { Claim } from './types/claim'
import type { ClaimFormValues } from './components/CreateClaimDrawer'

const CreateClaimDrawer = lazy(() => import('./components/CreateClaimDrawer').then(({ CreateClaimDrawer }) => ({ default: CreateClaimDrawer })))

function App() {
  const [isCreateClaimOpen, setIsCreateClaimOpen] = useState(false)
  const [selectedClaim, setSelectedClaim] = useState<Claim | null>(null)
  const [allClaims, setAllClaims] = useState<Claim[]>([])
  const [totalClaims, setTotalClaims] = useState(0)
  const [currentPage, setCurrentPage] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [metrics, setMetrics] = useState<Awaited<ReturnType<typeof getClaimMetrics>> | null>(null)
  const [isLoadingClaims, setIsLoadingClaims] = useState(true)
  const filters = useClaimFilters()

  useEffect(() => {
    Promise.all([listClaims(currentPage, pageSize, filters.searchTerm, filters.statusFilter, filters.typeFilter), getClaimMetrics()])
      .then(([loadedPage, loadedMetrics]) => {
        setAllClaims(loadedPage.content)
        setTotalClaims(loadedPage.totalElements)
        setMetrics(loadedMetrics)
      })
      .catch((error: unknown) => message.error(error instanceof Error ? error.message : 'Unable to load claims.'))
      .finally(() => setIsLoadingClaims(false))
  }, [currentPage, pageSize, filters.searchTerm, filters.statusFilter, filters.typeFilter])

  const handleSearchChange = (value: string) => {
    setCurrentPage(0)
    filters.setSearchTerm(value)
  }

  const handleStatusChange = (value: string) => {
    setCurrentPage(0)
    filters.setStatusFilter(value)
  }

  const handleTypeChange = (value: string) => {
    setCurrentPage(0)
    filters.setTypeFilter(value)
  }

  const handlePageSizeChange = (nextPageSize: number) => {
    setPageSize(nextPageSize)
    setCurrentPage(0)
  }

  const handleCreateClaim = async (values: ClaimFormValues) => {
    const request = {
      claimNumber: values.claimNumber,
      policyNumber: values.policyNumber,
      customerName: values.customerName,
      claimType: values.claimType,
      claimAmount: values.claimAmount,
      incidentDate: values.incidentDate.format('YYYY-MM-DD'),
      description: values.description,
    }
    const newClaim = await createClaim(request)

    setAllClaims((currentClaims) => [newClaim, ...currentClaims])
  }

  const handleClaimUpdated = async (updatedClaim: Claim) => {
    setAllClaims((currentClaims) => currentClaims.map((claim) => claim.id === updatedClaim.id ? updatedClaim : claim))
    setMetrics(await getClaimMetrics())
  }

  return (
    <ConfigProvider theme={{ token: { colorPrimary: '#df142d', borderRadius: 5, colorBgContainer: '#fcfcfd', colorBgElevated: '#fdfdfe' } }}>
      <main className="app-shell">
        <Sidebar />
        <section className="content">
          <DashboardHeader onCreateClaim={() => setIsCreateClaimOpen(true)} />
          <ClaimMetrics metrics={metrics} />
          <ClaimsTable
            claims={allClaims}
            searchTerm={filters.searchTerm}
            onSearchChange={handleSearchChange}
            statusFilter={filters.statusFilter}
            onStatusChange={handleStatusChange}
            typeFilter={filters.typeFilter}
            onTypeChange={handleTypeChange}
            loading={isLoadingClaims}
            onSelectClaim={setSelectedClaim}
            totalClaims={totalClaims}
            currentPage={currentPage}
            onPageChange={setCurrentPage}
            pageSize={pageSize}
            onPageSizeChange={handlePageSizeChange}
          />
        </section>
        <Suspense fallback={null}>
          <CreateClaimDrawer
            open={isCreateClaimOpen}
            onClose={() => setIsCreateClaimOpen(false)}
            onSubmit={handleCreateClaim}
          />
        </Suspense>
        <ClaimDetailsDrawer
          claim={selectedClaim}
          onClose={() => setSelectedClaim(null)}
          onUpdated={handleClaimUpdated}
        />
      </main>
    </ConfigProvider>
  )
}

export default App
