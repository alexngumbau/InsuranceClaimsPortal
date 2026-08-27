import { useState } from 'react'
import { Button, DatePicker, Drawer, Form, Input, InputNumber, Select, Space, message } from 'antd'
import type { Dayjs } from 'dayjs'
import { findPolicy } from '../api/claimsApi'
import type { ClaimType } from '../types/claim'

interface CreateClaimDrawerProps {
  open: boolean
  onClose: () => void
  onSubmit: (claim: ClaimFormValues) => Promise<void>
}

export interface ClaimFormValues {
  claimNumber: string
  policyNumber: string
  customerName: string
  claimType: ClaimType
  claimAmount: number
  incidentDate: Dayjs
  description: string
}

const claimTypeOptions = (['Motor', 'Health', 'Travel', 'Property', 'Other'] as ClaimType[])
  .map((type) => ({ value: type, label: type }))

export function CreateClaimDrawer({ open, onClose, onSubmit }: CreateClaimDrawerProps) {
  const [form] = Form.useForm<ClaimFormValues>()
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isLookingUpPolicy, setIsLookingUpPolicy] = useState(false)
  const [isPolicyVerified, setIsPolicyVerified] = useState(false)
  const [policyLookupError, setPolicyLookupError] = useState('')

  const clearPolicyDetails = () => {
    setIsPolicyVerified(false)
    setPolicyLookupError('')
    form.setFieldsValue({ customerName: undefined, claimType: undefined })
  }

  const handlePolicyChange = () => {
    clearPolicyDetails()
  }

  const handlePolicyLookup = async () => {
    const policyNumber = form.getFieldValue('policyNumber')?.trim()
    clearPolicyDetails()
    if (!policyNumber) return

    setIsLookingUpPolicy(true)
    try {
      const policy = await findPolicy(policyNumber)
      form.setFieldsValue({
        policyNumber: policy.policyNumber,
        customerName: policy.customerName,
        claimType: policy.policyType,
      })
      setIsPolicyVerified(true)
    } catch (error) {
      setPolicyLookupError(error instanceof Error ? error.message : 'Policy does not exist.')
    } finally {
      setIsLookingUpPolicy(false)
    }
  }

  const handleFinish = async (values: ClaimFormValues) => {
    if (!isPolicyVerified) {
      setPolicyLookupError('Verify the policy number before submitting.')
      return
    }

    setIsSubmitting(true)
    try {
      await onSubmit(values)
      message.success(`Claim ${values.claimNumber} created as submitted`)
      form.resetFields()
      setIsPolicyVerified(false)
      setPolicyLookupError('')
      onClose()
    } catch (error) {
      message.error(error instanceof Error ? error.message : 'Unable to create claim.')
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleClose = () => {
    form.resetFields()
    setIsPolicyVerified(false)
    setPolicyLookupError('')
    onClose()
  }

  return (
    <Drawer
      title={<span className="drawer-title">Create a new claim</span>}
      open={open}
      onClose={handleClose}
      size={480}
      destroyOnHidden
      footer={
        <Space>
          <Button onClick={handleClose}>Cancel</Button>
          <Button type="primary" onClick={() => form.submit()} loading={isSubmitting} disabled={isLookingUpPolicy || !isPolicyVerified}>
            Submit claim
          </Button>
        </Space>
      }
    >
      <p className="drawer-intro">Capture the claim details below.</p>
      <Form form={form} layout="vertical" onFinish={handleFinish} requiredMark="optional">
        <Form.Item
          name="claimNumber"
          label="Claim number"
          rules={[{ required: true, message: 'Enter a claim number' }]}
        >
          <Input placeholder="CLM-2026-0004" />
        </Form.Item>
        <Form.Item
          name="policyNumber"
          label="Policy number"
          validateStatus={policyLookupError ? 'error' : isPolicyVerified ? 'success' : undefined}
          help={policyLookupError || (isPolicyVerified ? 'Policy verified' : 'Enter a policy number and leave the field to verify it')}
          rules={[{ required: true, message: 'Enter a policy number' }]}
        >
          <Input
            placeholder="POL-2026-035"
            onChange={handlePolicyChange}
            onBlur={handlePolicyLookup}
            suffix={isLookingUpPolicy ? 'Checking...' : undefined}
          />
        </Form.Item>
        <Form.Item name="customerName" label="Customer name" rules={[{ required: true, message: 'Verify a policy first' }]}>
          <Input placeholder="Auto-populated from policy" readOnly />
        </Form.Item>
        <Form.Item name="claimType" label="Claim type" rules={[{ required: true, message: 'Verify a policy first' }]}>
          <Select placeholder="Auto-populated from policy" options={claimTypeOptions} disabled />
        </Form.Item>
        <Form.Item name="claimAmount" label="Claim amount" rules={[{ required: true, message: 'Enter the claim amount' }]}>
          <InputNumber min={1} precision={2} prefix="KES" placeholder="0.00" style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="incidentDate" label="Incident date" rules={[{ required: true, message: 'Select the incident date' }]}>
          <DatePicker style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="description" label="Description" rules={[{ required: true, min: 10, message: 'Provide at least 10 characters' }]}>
          <Input.TextArea rows={4} placeholder="Briefly describe what happened" />
        </Form.Item>
      </Form>
    </Drawer>
  )
}
