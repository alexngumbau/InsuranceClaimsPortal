package com.jubilee.InsuranceClaimsBE.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.jubilee.InsuranceClaimsBE.domain.ClaimStatus;
import com.jubilee.InsuranceClaimsBE.dto.ClaimResponse;
import com.jubilee.InsuranceClaimsBE.dto.ClaimMetricsResponse;
import com.jubilee.InsuranceClaimsBE.dto.CreateClaimRequest;
import com.jubilee.InsuranceClaimsBE.dto.UpdateStatusRequest;
import com.jubilee.InsuranceClaimsBE.service.ClaimService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {
    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClaimResponse create(@Valid @RequestBody CreateClaimRequest request) {
        return claimService.create(request);
    }

    @GetMapping
    public Page<ClaimResponse> find(@RequestParam(required = false) String search,
                                    @RequestParam(required = false) ClaimStatus status,
                                    @RequestParam(required = false) String type,
                                    @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return claimService.find(search, status, type, pageable);
    }

    @GetMapping("/metrics")
    public ClaimMetricsResponse metrics() {
        return claimService.metrics();
    }

    @GetMapping("/{id}")
    public ClaimResponse findById(@PathVariable Long id) {
        return claimService.findById(id);
    }

    @PatchMapping("/{id}/status")
    public ClaimResponse changeStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
        return claimService.changeStatus(id, request.getStatus());
    }
}
