package com.jubilee.InsuranceClaimsBE.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.jubilee.InsuranceClaimsBE.dto.PolicyResponse;
import com.jubilee.InsuranceClaimsBE.service.ClaimService;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {
    private final ClaimService claimService;

    public PolicyController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @GetMapping("/{policyNumber}")
    public PolicyResponse findByPolicyNumber(@PathVariable String policyNumber) {
        return claimService.findPolicy(policyNumber);
    }
}
