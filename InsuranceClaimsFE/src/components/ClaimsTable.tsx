import { useEffect, useState } from 'react'
import { MoreOutlined, SearchOutlined } from '@ant-design/icons'
import { Button, Descriptions, Drawer, Input, Pagination, Select, Table, Tag, message } from 'antd'
import type { TableColumnsType } from 'antd'
import { getClaim, updateClaimStatus } from '../api/claimsApi'
import type { Claim, ClaimStatus, ClaimType } from '../types/claim'

interface ClaimsTableProps {
  claims: Claim[]
  searchTerm: string
  onSearchChange: (value: string) => void
  statusFilter: string
  onStatusChange: (value: string) => void
  typeFilter: string
  onTypeChange: (value: string) => void
  loading?: boolean
  onSelectClaim: (claim: Claim) => void
  totalClaims: number
  currentPage: number
  onPageChange: (page: number) => void
  pageSize: number
  onPageSizeChange: (pageSize: number) => void
}

const statusColors: Record<ClaimStatus, string> = {
  SUBMITTED: 'orange',
  UNDER_REVIEW: 'blue',
  APPROVED: 'green',
  REJECTED: 'red',
  PAID: 'green',
}

const createColumns = (onSelectClaim: (claim: Claim) => void): TableColumnsType<Claim> => [
  { title: 'Claim number', dataIndex: 'number', key: 'number', render: (value) => <span className="claim-number">{value}</span> },
  { title: 'Customer', dataIndex: 'customer', key: 'customer' },
  { title: 'Policy number', dataIndex: 'policy', key: 'policy' },
  { title: 'Type', dataIndex: 'type', key: 'type' },
  { title: 'Amount', dataIndex: 'amount', key: 'amount' },
  {
    title: 'Status',
    dataIndex: 'status',
    key: 'status',
    render: (status: ClaimStatus) => <Tag color={statusColors[status]}>{status.replace('_', ' ')}</Tag>,
  },
  { key: 'actions', render: (_, claim) => <Button type="text" icon={<MoreOutlined />} onClick={() => onSelectClaim(claim)} aria-label={`Actions for ${claim.number}`} /> },
]

const statusOptions = [
  { value: 'all', label: 'All statuses' },
  { value: 'submitted', label: 'Submitted' },
  { value: 'under-review', label: 'Under review' },
  { value: 'approved', label: 'Approved' },
  { value: 'paid', label: 'Paid' },
]

const typeOptions: { value: string; label: string }[] = [
  { value: 'all-types', label: 'All claim types' },
  ...(['Motor', 'Health', 'Travel', 'Property', 'Other'] as ClaimType[]).map((type) => ({
    value: type.toLowerCase(),
    label: type,
  })),
]

export function ClaimsTable({
  claims,
  searchTerm,
  onSearchChange,
  statusFilter,
  onStatusChange,
  typeFilter,
  onTypeChange,
  loading = false,
  onSelectClaim,
  totalClaims,
  currentPage,
  onPageChange,
  pageSize,
  onPageSizeChange,
}: ClaimsTableProps) {
  return (
    <section className="claims-section" id="claims">
      <div className="section-heading">
        <div><h3>Recent claims</h3><p className="muted">Review the latest activity in your portfolio.</p></div>
        <button className="text-button" type="button">View all claims →</button>
      </div>

      <div className="toolbar">
        <Input
          className="search-field"
          prefix={<SearchOutlined />}
          value={searchTerm}
          onChange={(event) => onSearchChange(event.target.value)}
          placeholder="Search claim, policy or customer"
          allowClear
        />
        <Select className="filter-select" value={statusFilter} onChange={onStatusChange} options={statusOptions} aria-label="Filter by status" />
        <Select className="filter-select" value={typeFilter} onChange={onTypeChange} options={typeOptions} aria-label="Filter by claim type" />
      </div>

      <div className="table-wrapper">
        <Table<Claim>
          rowKey="number"
          columns={createColumns(onSelectClaim)}
          dataSource={claims}
          loading={loading}
          pagination={false}
          locale={{ emptyText: 'No claims match your filters.' }}
        />
      </div>

      <div className="table-footer">
        <span>Showing {claims.length} of {totalClaims} matching claims</span>
        <Pagination
          current={currentPage + 1}
          pageSize={pageSize}
          total={totalClaims}
          showSizeChanger
          pageSizeOptions={[10, 20, 30, 50, 100, 500, 1000]}
          showTotal={(total, range) => `${range[0]}-${range[1]} of ${total}`}
          onChange={(page, nextPageSize) => {
            if (nextPageSize !== pageSize) onPageSizeChange(nextPageSize)
            onPageChange(page - 1)
          }}
        />
      </div>
    </section>
  )
}

interface ClaimDetailsDrawerProps {
  claim: Claim | null
  onClose: () => void
  onUpdated: (claim: Claim) => void
}

const nextStatuses: Record<ClaimStatus, ClaimStatus[]> = {
  SUBMITTED: ['UNDER_REVIEW'], UNDER_REVIEW: ['APPROVED', 'REJECTED'], APPROVED: ['PAID'], REJECTED: [], PAID: [],
}

const detailStatusColors: Record<ClaimStatus, string> = {
  SUBMITTED: 'orange', UNDER_REVIEW: 'blue', APPROVED: 'green', REJECTED: 'red', PAID: 'green',
}

export function ClaimDetailsDrawer({ claim, onClose, onUpdated }: ClaimDetailsDrawerProps) {
  const [details, setDetails] = useState<Claim | null>(claim)
  const [selectedStatus, setSelectedStatus] = useState<ClaimStatus>()
  const [isUpdating, setIsUpdating] = useState(false)

  useEffect(() => {
    if (!claim) return
    getClaim(claim.id)
      .then(setDetails)
      .catch((error: unknown) => message.error(error instanceof Error ? error.message : 'Unable to load claim.'))
  }, [claim])

  const handleStatusUpdate = async () => {
    if (!details || !selectedStatus) return
    setIsUpdating(true)
    try {
      const updatedClaim = await updateClaimStatus(details.id, selectedStatus)
      setDetails(updatedClaim)
      setSelectedStatus(undefined)
      onUpdated(updatedClaim)
      message.success(`Claim ${updatedClaim.number} updated to ${updatedClaim.status.replace('_', ' ')}`)
    } catch (error) {
      message.error(error instanceof Error ? error.message : 'Unable to update claim status.')
    } finally {
      setIsUpdating(false)
    }
  }

  const visibleDetails = details?.id === claim?.id ? details : claim
  const availableStatuses = visibleDetails ? nextStatuses[visibleDetails.status] : []
  const visibleSelectedStatus = availableStatuses.includes(selectedStatus as ClaimStatus) ? selectedStatus : undefined

  return (
    <Drawer title={<span className="drawer-title">Claim details</span>} open={Boolean(claim)} onClose={onClose} size={480}>
      {visibleDetails && <Descriptions column={1} bordered size="small">
        <Descriptions.Item label="Claim number">{visibleDetails.number}</Descriptions.Item>
        <Descriptions.Item label="Customer">{visibleDetails.customer}</Descriptions.Item>
        <Descriptions.Item label="Policy number">{visibleDetails.policy}</Descriptions.Item>
        <Descriptions.Item label="Claim type">{visibleDetails.type}</Descriptions.Item>
        <Descriptions.Item label="Amount">{visibleDetails.amount}</Descriptions.Item>
        <Descriptions.Item label="Incident date">{visibleDetails.incidentDate || 'Not available'}</Descriptions.Item>
        <Descriptions.Item label="Description">{visibleDetails.description || 'Not available'}</Descriptions.Item>
        <Descriptions.Item label="Status"><Tag color={detailStatusColors[visibleDetails.status]}>{visibleDetails.status.replace('_', ' ')}</Tag></Descriptions.Item>
      </Descriptions>}
      {visibleDetails && availableStatuses.length > 0 && <div className="status-actions">
        <Select placeholder="Select next status" value={visibleSelectedStatus} onChange={setSelectedStatus} options={availableStatuses.map((status) => ({ value: status, label: status.replace('_', ' ') }))} />
        <Button type="primary" disabled={!selectedStatus} loading={isUpdating} onClick={handleStatusUpdate}>Update status</Button>
      </div>}
    </Drawer>
  )
}
