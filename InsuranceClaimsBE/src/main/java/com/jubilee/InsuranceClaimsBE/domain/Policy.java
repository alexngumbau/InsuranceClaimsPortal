package com.jubilee.InsuranceClaimsBE.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "policies", uniqueConstraints = @UniqueConstraint(columnNames = "policyNumber"))
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Policy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String policyNumber;
    private String customerName;
    private String policyType;

    public Policy(String policyNumber, String customerName, String policyType) {
        this.policyNumber = policyNumber;
        this.customerName = customerName;
        this.policyType = policyType;
    }

}
