import type { Claim } from '../types/claim'

export interface CreateClaimRequest {
  claimNumber: string
  policyNumber: string
  customerName: string
  claimType: Claim['type']
  claimAmount: number
  incidentDate: string
  description: string
}

export interface ClaimMetrics {
  totalClaims: number
  pendingReview: number
  approvedAmount: number
  paidClaims: number
}

export interface ClaimPage {
  content: Claim[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface Policy {
  policyNumber: string
  customerName: string
  policyType: Claim['type']
}

interface ApiErrorResponse {
  message?: string
}

const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'

async function getApiError(response: Response, fallback: string): Promise<Error> {
  try {
    const error = await response.json() as ApiErrorResponse
    return new Error(error.message || fallback)
  } catch {
    return new Error(fallback)
  }
}

function toClaim(response: Omit<Claim, 'amount'> & { amount: number | string }): Claim {
  const amount = typeof response.amount === 'number'
    ? `KES ${response.amount.toLocaleString('en-KE')}`
    : response.amount
  return { ...response, amount }
}

export async function listClaims(page = 0, size = 10, search = '', status = 'all', type = 'all-types'): Promise<ClaimPage> {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  if (search.trim()) params.set('search', search.trim())
  if (status !== 'all') params.set('status', status.replace('-', '_').toUpperCase())
  if (type !== 'all-types') params.set('type', type)
  const response = await fetch(`${apiUrl}/api/claims?${params}`)
  if (!response.ok) throw await getApiError(response, `Unable to load claims. Server returned ${response.status}.`)
  const result = await response.json() as Omit<ClaimPage, 'content'> & { content: Array<Omit<Claim, 'amount'> & { amount: number | string }> }
  return { ...result, content: result.content.map(toClaim) }
}

export async function getClaimMetrics(): Promise<ClaimMetrics> {
  const response = await fetch(`${apiUrl}/api/claims/metrics`)
  if (!response.ok) throw await getApiError(response, `Unable to load claim metrics. Server returned ${response.status}.`)
  return response.json() as Promise<ClaimMetrics>
}

export async function findPolicy(policyNumber: string): Promise<Policy> {
  const response = await fetch(`${apiUrl}/api/policies/${encodeURIComponent(policyNumber)}`)
  if (!response.ok) throw await getApiError(response, `Unable to find policy. Server returned ${response.status}.`)
  return response.json() as Promise<Policy>
}

export async function getClaim(id: number): Promise<Claim> {
  const response = await fetch(`${apiUrl}/api/claims/${id}`)
  if (!response.ok) throw await getApiError(response, `Unable to load claim. Server returned ${response.status}.`)
  return toClaim(await response.json() as Omit<Claim, 'amount'> & { amount: number | string })
}

export async function updateClaimStatus(id: number, status: Claim['status']): Promise<Claim> {
  const response = await fetch(`${apiUrl}/api/claims/${id}/status`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ status }),
  })
  if (!response.ok) throw await getApiError(response, `Unable to update claim status. Server returned ${response.status}.`)
  return toClaim(await response.json() as Omit<Claim, 'amount'> & { amount: number | string })
}

export async function createClaim(request: CreateClaimRequest): Promise<Claim> {
  const response = await fetch(`${apiUrl}/api/claims`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })

  if (!response.ok) {
    throw await getApiError(response, `Unable to create claim. Server returned ${response.status}.`)
  }

  return toClaim(await response.json() as Omit<Claim, 'amount'> & { amount: number | string })
}
