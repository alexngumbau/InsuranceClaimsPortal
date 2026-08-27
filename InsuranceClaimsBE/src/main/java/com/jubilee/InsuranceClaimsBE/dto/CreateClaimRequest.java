package com.jubilee.InsuranceClaimsBE.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.jubilee.InsuranceClaimsBE.domain.ClaimType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateClaimRequest {
    @NotBlank private String claimNumber;
    @NotBlank private String policyNumber;
    @NotBlank private String customerName;
    @NotNull private ClaimType claimType;
    @NotNull @DecimalMin("0.01") private BigDecimal claimAmount;
    @NotNull private LocalDate incidentDate;
    @NotBlank @Size(min = 10, max = 1000) private String description;
}
