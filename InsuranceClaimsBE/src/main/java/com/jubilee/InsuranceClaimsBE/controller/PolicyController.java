package com.jubilee.InsuranceClaimsBE.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.jubilee.InsuranceClaimsBE.dto.PolicyResponse;
import com.jubilee.InsuranceClaimsBE.service.ClaimService;

@RestController
@RequestMapping("/api/policies")
@Tag(name = "Policies", description = "Policy lookup operations used during claim processing")
public class PolicyController {
    private final ClaimService claimService;

    public PolicyController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @GetMapping("/{policyNumber}")
    @Operation(summary = "Find a policy", description = "Returns policy details for claim validation.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Policy returned"),
        @ApiResponse(responseCode = "404", description = "Policy not found")
    })
    public PolicyResponse findByPolicyNumber(@PathVariable String policyNumber) {
        return claimService.findPolicy(policyNumber);
    }
}
