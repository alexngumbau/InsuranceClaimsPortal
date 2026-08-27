import { useState } from 'react'
import type { Claim } from '../types/claim'

export function useClaimFilters(claims: Claim[]) {
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState('all')
  const [typeFilter, setTypeFilter] = useState('all-types')

  const filteredClaims = claims.filter((claim) => {
    const normalizedSearch = searchTerm.trim().toLowerCase()
    const matchesSearch = [claim.number, claim.policy, claim.customer, claim.type]
      .some((value) => value.toLowerCase().includes(normalizedSearch))
    const matchesStatus = statusFilter === 'all'
      || claim.status.toLowerCase() === statusFilter.replace('-', '_')
    const matchesType = typeFilter === 'all-types'
      || claim.type.toLowerCase() === typeFilter

    return matchesSearch && matchesStatus && matchesType
  })

  return {
    filteredClaims,
    searchTerm,
    setSearchTerm,
    statusFilter,
    setStatusFilter,
    typeFilter,
    setTypeFilter,
  }
}
