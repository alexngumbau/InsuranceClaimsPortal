package com.jubilee.InsuranceClaimsBE.dto;

import com.jubilee.InsuranceClaimsBE.domain.ClaimStatus;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequest {
	@NotNull private ClaimStatus status;
}
