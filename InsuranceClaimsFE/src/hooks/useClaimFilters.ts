import { useState } from 'react'

// Holds the claims toolbar filter state. Filtering itself is performed
// server-side by the API (see listClaims), so no client-side claim list is kept here.
export function useClaimFilters() {
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState('all')
  const [typeFilter, setTypeFilter] = useState('all-types')

  return {
    searchTerm,
    setSearchTerm,
    statusFilter,
    setStatusFilter,
    typeFilter,
    setTypeFilter,
  }
}
