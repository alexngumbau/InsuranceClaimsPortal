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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.jubilee.InsuranceClaimsBE.domain.ClaimStatus;
import com.jubilee.InsuranceClaimsBE.dto.ClaimResponse;
import com.jubilee.InsuranceClaimsBE.dto.ClaimMetricsResponse;
import com.jubilee.InsuranceClaimsBE.dto.CreateClaimRequest;
import com.jubilee.InsuranceClaimsBE.dto.UpdateStatusRequest;
import com.jubilee.InsuranceClaimsBE.service.ClaimService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/claims")
@Tag(name = "Claims", description = "Create, search, inspect, and process insurance claims")
public class ClaimController {
    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a claim", description = "Creates a claim linked to an existing policy with SUBMITTED status.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Claim created"),
        @ApiResponse(responseCode = "400", description = "Invalid claim data or policy mismatch", content = @Content(schema = @Schema(implementation = com.jubilee.InsuranceClaimsBE.exception.ApiExceptionHandler.ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Policy not found"),
        @ApiResponse(responseCode = "409", description = "Claim number already exists")
    })
    public ClaimResponse create(@Valid @RequestBody CreateClaimRequest request) {
        return claimService.create(request);
    }

    @GetMapping
    @Operation(summary = "List claims", description = "Returns a paginated list of claims with optional search, status, and type filters.")
    public Page<ClaimResponse> find(@Parameter(description = "Search claim number, policy number, or customer name") @RequestParam(required = false) String search,
                                    @Parameter(description = "Filter by claim status") @RequestParam(required = false) ClaimStatus status,
                                    @Parameter(description = "Filter by claim type") @RequestParam(required = false) String type,
                                    @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return claimService.find(search, status, type, pageable);
    }

    @GetMapping("/metrics")
    @Operation(summary = "Get claim metrics", description = "Returns dashboard totals for claims, pending review, approved amount, and paid claims.")
    public ClaimMetricsResponse metrics() {
        return claimService.metrics();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get claim details")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Claim details returned"),
        @ApiResponse(responseCode = "404", description = "Claim not found")
    })
    public ClaimResponse findById(@PathVariable Long id) {
        return claimService.findById(id);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change claim status", description = "Advances a claim through the allowed status workflow.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status updated"),
        @ApiResponse(responseCode = "404", description = "Claim not found"),
        @ApiResponse(responseCode = "409", description = "Invalid status transition")
    })
    public ClaimResponse changeStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
        return claimService.changeStatus(id, request.getStatus());
    }
}
