package com.jubilee.InsuranceClaimsBE.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.jubilee.InsuranceClaimsBE.domain.Claim;
import com.jubilee.InsuranceClaimsBE.domain.ClaimStatus;
import com.jubilee.InsuranceClaimsBE.domain.ClaimType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClaimResponse {
    private Long id;
    private String number;
    private String customer;
    private String policy;
    private ClaimType type;
    private BigDecimal amount;
    private LocalDate incidentDate;
    private String description;
    private ClaimStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /* fields are intentionally exposed through Lombok getters for Jackson */
    public static ClaimResponse from(Claim claim) {
        return new ClaimResponse(claim.getId(), claim.getClaimNumber(), claim.getPolicy().getCustomerName(),
            claim.getPolicy().getPolicyNumber(), claim.getClaimType(), claim.getClaimAmount(),
            claim.getIncidentDate(), claim.getDescription(), claim.getStatus(),
            claim.getCreatedAt(), claim.getUpdatedAt());
    }
}
