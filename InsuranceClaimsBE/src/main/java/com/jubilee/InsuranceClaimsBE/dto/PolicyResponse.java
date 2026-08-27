package com.jubilee.InsuranceClaimsBE.dto;

import com.jubilee.InsuranceClaimsBE.domain.Policy;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PolicyResponse {
    private String policyNumber;
    private String customerName;
    private String policyType;

    public static PolicyResponse from(Policy policy) {
        return new PolicyResponse(policy.getPolicyNumber(), policy.getCustomerName(), policy.getPolicyType());
    }
}
