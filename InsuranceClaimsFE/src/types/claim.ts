export type ClaimStatus = 'SUBMITTED' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED' | 'PAID'

export type ClaimType = 'Motor' | 'Health' | 'Travel' | 'Property' | 'Other'

export interface Claim {
  id: number
  number: string
  customer: string
  policy: string
  type: ClaimType
  amount: string
  incidentDate?: string
  description?: string
  status: ClaimStatus
}
