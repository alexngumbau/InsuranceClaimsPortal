package com.jubilee.InsuranceClaimsBE.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClaimMetricsResponse {
    private long totalClaims;
    private long pendingReview;
    private BigDecimal approvedAmount;
    private long paidClaims;
}
