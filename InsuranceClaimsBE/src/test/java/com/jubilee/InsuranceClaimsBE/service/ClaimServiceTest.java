package com.jubilee.InsuranceClaimsBE.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.jubilee.InsuranceClaimsBE.domain.Claim;
import com.jubilee.InsuranceClaimsBE.domain.ClaimStatus;
import com.jubilee.InsuranceClaimsBE.domain.ClaimType;
import com.jubilee.InsuranceClaimsBE.domain.Policy;
import com.jubilee.InsuranceClaimsBE.dto.ClaimResponse;
import com.jubilee.InsuranceClaimsBE.dto.CreateClaimRequest;
import com.jubilee.InsuranceClaimsBE.repository.ClaimRepository;
import com.jubilee.InsuranceClaimsBE.repository.PolicyRepository;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {
    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private PolicyRepository policyRepository;

    private ClaimService claimService;
    private Policy motorPolicy;

    @BeforeEach
    void setUp() {
        claimService = new ClaimService(claimRepository, policyRepository);
        motorPolicy = new Policy("POL-2026-001", "Jane Doe", "Motor");
    }

    @Test
    void createsClaimWithSubmittedStatusWhenRequestMatchesPolicy() {
        CreateClaimRequest request = validRequest();
        when(claimRepository.existsByClaimNumber(request.getClaimNumber())).thenReturn(false);
        when(policyRepository.findByPolicyNumber(request.getPolicyNumber())).thenReturn(Optional.of(motorPolicy));
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClaimResponse response = claimService.create(request);

        assertEquals(request.getClaimNumber(), response.getNumber());
        assertEquals(ClaimStatus.SUBMITTED, response.getStatus());
        assertEquals(request.getClaimAmount(), response.getAmount());
        verify(claimRepository).save(any(Claim.class));
    }

    @Test
    void rejectsDuplicateClaimNumber() {
        CreateClaimRequest request = validRequest();
        when(claimRepository.existsByClaimNumber(request.getClaimNumber())).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> claimService.create(request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(policyRepository, never()).findByPolicyNumber(any());
    }

    @Test
    void rejectsClaimWhenPolicyDoesNotExist() {
        CreateClaimRequest request = validRequest();
        when(claimRepository.existsByClaimNumber(request.getClaimNumber())).thenReturn(false);
        when(policyRepository.findByPolicyNumber(request.getPolicyNumber())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> claimService.create(request));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void rejectsClaimWhenCustomerDoesNotMatchPolicy() {
        CreateClaimRequest request = new CreateClaimRequest(
            "CLM-2026-0001", "POL-2026-001", "John Doe", ClaimType.Motor,
            new BigDecimal("250000"), LocalDate.of(2026, 8, 15),
            "Vehicle damage following an accident.");
        when(claimRepository.existsByClaimNumber(request.getClaimNumber())).thenReturn(false);
        when(policyRepository.findByPolicyNumber(request.getPolicyNumber())).thenReturn(Optional.of(motorPolicy));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> claimService.create(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(claimRepository, never()).save(any(Claim.class));
    }

    @Test
    void rejectsClaimWhenTypeDoesNotMatchPolicy() {
        CreateClaimRequest request = new CreateClaimRequest(
            "CLM-2026-0001", "POL-2026-001", "Jane Doe", ClaimType.Health,
            new BigDecimal("250000"), LocalDate.of(2026, 8, 15),
            "Medical treatment following an incident.");
        when(claimRepository.existsByClaimNumber(request.getClaimNumber())).thenReturn(false);
        when(policyRepository.findByPolicyNumber(request.getPolicyNumber())).thenReturn(Optional.of(motorPolicy));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> claimService.create(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(claimRepository, never()).save(any(Claim.class));
    }

    @Test
    void allowsSubmittedToUnderReviewTransition() {
        Claim claim = claim(ClaimStatus.SUBMITTED);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClaimResponse response = claimService.changeStatus(1L, ClaimStatus.UNDER_REVIEW);

        assertEquals(ClaimStatus.UNDER_REVIEW, response.getStatus());
        verify(claimRepository).save(claim);
    }

    @Test
    void allowsUnderReviewToApprovedAndRejectedTransitions() {
        Claim approvedClaim = claim(ClaimStatus.UNDER_REVIEW);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(approvedClaim));
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(ClaimStatus.APPROVED, claimService.changeStatus(1L, ClaimStatus.APPROVED).getStatus());

        Claim rejectedClaim = claim(ClaimStatus.UNDER_REVIEW);
        when(claimRepository.findById(2L)).thenReturn(Optional.of(rejectedClaim));
        assertEquals(ClaimStatus.REJECTED, claimService.changeStatus(2L, ClaimStatus.REJECTED).getStatus());
    }

    @Test
    void allowsApprovedToPaidTransition() {
        Claim claim = claim(ClaimStatus.APPROVED);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClaimResponse response = claimService.changeStatus(1L, ClaimStatus.PAID);

        assertEquals(ClaimStatus.PAID, response.getStatus());
    }

    @Test
    void rejectsInvalidStatusTransition() {
        Claim claim = claim(ClaimStatus.SUBMITTED);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> claimService.changeStatus(1L, ClaimStatus.APPROVED));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(claimRepository, never()).save(any(Claim.class));
    }

    private CreateClaimRequest validRequest() {
        return new CreateClaimRequest(
            "CLM-2026-0001", "POL-2026-001", "Jane Doe", ClaimType.Motor,
            new BigDecimal("250000"), LocalDate.of(2026, 8, 15),
            "Vehicle damage following an accident.");
    }

    private Claim claim(ClaimStatus status) {
        return Claim.builder()
            .id(1L)
            .claimNumber("CLM-2026-0001")
            .policy(motorPolicy)
            .claimType(ClaimType.Motor)
            .claimAmount(new BigDecimal("250000"))
            .incidentDate(LocalDate.of(2026, 8, 15))
            .description("Vehicle damage following an accident.")
            .status(status)
            .build();
    }
}
