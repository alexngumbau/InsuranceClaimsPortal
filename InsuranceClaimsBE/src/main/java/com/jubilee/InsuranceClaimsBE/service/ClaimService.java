package com.jubilee.InsuranceClaimsBE.service;

import java.util.List;
import java.util.Locale;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.jubilee.InsuranceClaimsBE.domain.Claim;
import com.jubilee.InsuranceClaimsBE.domain.ClaimStatus;
import com.jubilee.InsuranceClaimsBE.domain.Policy;
import com.jubilee.InsuranceClaimsBE.dto.ClaimResponse;
import com.jubilee.InsuranceClaimsBE.dto.ClaimMetricsResponse;
import com.jubilee.InsuranceClaimsBE.dto.PolicyResponse;
import com.jubilee.InsuranceClaimsBE.dto.CreateClaimRequest;
import com.jubilee.InsuranceClaimsBE.repository.ClaimRepository;
import com.jubilee.InsuranceClaimsBE.repository.PolicyRepository;

@Service
public class ClaimService {
    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;

    public ClaimService(ClaimRepository claimRepository, PolicyRepository policyRepository) {
        this.claimRepository = claimRepository;
        this.policyRepository = policyRepository;
    }

    @Transactional
    public ClaimResponse create(CreateClaimRequest request) {
        if (claimRepository.existsByClaimNumber(request.getClaimNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Claim number already exists.");
        }
        Policy policy = policyRepository.findByPolicyNumber(request.getPolicyNumber())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Policy does not exist."));
        if (!policy.getCustomerName().equalsIgnoreCase(request.getCustomerName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer does not match the policy.");
        }
        if (!policy.getPolicyType().equalsIgnoreCase(request.getClaimType().name())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Claim type must match the policy type: " + policy.getPolicyType() + ".");
        }
        Claim claim = Claim.builder()
            .claimNumber(request.getClaimNumber())
            .policy(policy)
            .claimType(request.getClaimType())
            .claimAmount(request.getClaimAmount())
            .incidentDate(request.getIncidentDate())
            .description(request.getDescription())
            .build();
        return ClaimResponse.from(claimRepository.save(claim));
    }

    @Transactional(readOnly = true)
    public Page<ClaimResponse> find(String search, ClaimStatus status, String type, Pageable pageable) {
        String normalizedSearch = search == null ? "" : search.toLowerCase(Locale.ROOT).trim();
        Specification<Claim> specification = (root, query, builder) -> builder.conjunction();
        if (!normalizedSearch.isBlank()) {
            specification = specification.and((root, query, builder) -> {
                var policy = root.join("policy");
                String pattern = "%" + normalizedSearch + "%";
                return builder.or(
                    builder.like(builder.lower(root.get("claimNumber")), pattern),
                    builder.like(builder.lower(policy.get("policyNumber")), pattern),
                    builder.like(builder.lower(policy.get("customerName")), pattern));
            });
        }
        if (status != null) specification = specification.and((root, query, builder) -> builder.equal(root.get("status"), status));
        if (type != null && !type.isBlank()) specification = specification.and((root, query, builder) -> builder.equal(root.get("claimType"), com.jubilee.InsuranceClaimsBE.domain.ClaimType.valueOf(type.substring(0, 1).toUpperCase(Locale.ROOT) + type.substring(1).toLowerCase(Locale.ROOT))));
        return claimRepository.findAll(specification, pageable).map(ClaimResponse::from);
    }

    @Transactional(readOnly = true)
    public ClaimResponse findById(Long id) {
        return ClaimResponse.from(claimRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Claim does not exist.")));
    }

    @Transactional(readOnly = true)
    public ClaimMetricsResponse metrics() {
        List<Claim> claims = claimRepository.findAll();
        BigDecimal approvedAmount = claims.stream()
            .filter(claim -> claim.getStatus() == ClaimStatus.APPROVED || claim.getStatus() == ClaimStatus.PAID)
            .map(Claim::getClaimAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        long pendingReview = claims.stream()
            .filter(claim -> claim.getStatus() == ClaimStatus.SUBMITTED || claim.getStatus() == ClaimStatus.UNDER_REVIEW)
            .count();
        long paidClaims = claims.stream().filter(claim -> claim.getStatus() == ClaimStatus.PAID).count();
        return new ClaimMetricsResponse(claims.size(), pendingReview, approvedAmount, paidClaims);
    }

    @Transactional(readOnly = true)
    public PolicyResponse findPolicy(String policyNumber) {
        return PolicyResponse.from(policyRepository.findByPolicyNumber(policyNumber)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Policy does not exist.")));
    }

    @Transactional
    public ClaimResponse changeStatus(Long id, ClaimStatus nextStatus) {
        Claim claim = claimRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Claim does not exist."));
        if (!isAllowed(claim.getStatus(), nextStatus)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid claim status transition.");
        }
        claim.changeStatus(nextStatus);
        return ClaimResponse.from(claimRepository.save(claim));
    }

    private boolean isAllowed(ClaimStatus current, ClaimStatus next) {
        return (current == ClaimStatus.SUBMITTED && next == ClaimStatus.UNDER_REVIEW)
            || (current == ClaimStatus.UNDER_REVIEW && (next == ClaimStatus.APPROVED || next == ClaimStatus.REJECTED))
            || (current == ClaimStatus.APPROVED && next == ClaimStatus.PAID);
    }
}
