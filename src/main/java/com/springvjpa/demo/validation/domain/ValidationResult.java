package com.springvjpa.demo.validation.domain;

import com.springvjpa.demo.law.domain.LawReference;
import com.springvjpa.demo.policy.domain.Policy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "validation_results")
public class ValidationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "policy_id")
    private Policy policy;

    @ManyToOne(optional = false)
    @JoinColumn(name = "law_reference_id")
    private LawReference lawReference;

    @Column(nullable = false)
    private String result;

    @Column(length = 2000)
    private String remarks;

    @Column(nullable = false)
    private LocalDate checkedOn;

    protected ValidationResult() {
    }

    public ValidationResult(Policy policy, LawReference lawReference, String result, String remarks, LocalDate checkedOn) {
        this.policy = policy;
        this.lawReference = lawReference;
        this.result = result;
        this.remarks = remarks;
        this.checkedOn = checkedOn;
    }

    public Long getId() {
        return id;
    }

    public Policy getPolicy() {
        return policy;
    }

    public LawReference getLawReference() {
        return lawReference;
    }

    public String getResult() {
        return result;
    }

    public String getRemarks() {
        return remarks;
    }

    public LocalDate getCheckedOn() {
        return checkedOn;
    }
}
