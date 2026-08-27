package com.jubilee.InsuranceClaimsBE.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.jubilee.InsuranceClaimsBE.domain.Claim;

public interface ClaimRepository extends JpaRepository<Claim, Long>, JpaSpecificationExecutor<Claim> {
    boolean existsByClaimNumber(String claimNumber);
}
