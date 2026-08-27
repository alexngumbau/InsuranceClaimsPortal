package com.jubilee.InsuranceClaimsBE.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.jubilee.InsuranceClaimsBE.domain.Claim;
import com.jubilee.InsuranceClaimsBE.domain.ClaimType;
import com.jubilee.InsuranceClaimsBE.domain.Policy;
import com.jubilee.InsuranceClaimsBE.repository.ClaimRepository;
import com.jubilee.InsuranceClaimsBE.repository.PolicyRepository;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedData(PolicyRepository policyRepository, ClaimRepository claimRepository) {
        return args -> {
            if (policyRepository.count() > 0) return;
            Policy motor = policyRepository.save(new Policy("POL-2026-001", "Jane Doe", "Motor"));
            Policy health = policyRepository.save(new Policy("POL-2026-014", "Brian Otieno", "Health"));
            Policy travel = policyRepository.save(new Policy("POL-2026-027", "Amina Hassan", "Travel"));
            claimRepository.save(Claim.builder().claimNumber("CLM-2026-0001").policy(motor).claimType(ClaimType.Motor)
                .claimAmount(new BigDecimal("250000")).incidentDate(LocalDate.of(2026, 8, 15))
                .description("Vehicle damage following an accident.").build());
            claimRepository.save(Claim.builder().claimNumber("CLM-2026-0002").policy(health).claimType(ClaimType.Health)
                .claimAmount(new BigDecimal("84500")).incidentDate(LocalDate.of(2026, 8, 12))
                .description("Medical treatment claim.").build());
            claimRepository.save(Claim.builder().claimNumber("CLM-2026-0003").policy(travel).claimType(ClaimType.Travel)
                .claimAmount(new BigDecimal("42000")).incidentDate(LocalDate.of(2026, 8, 10))
                .description("Delayed baggage claim.").build());
        };
    }
}
