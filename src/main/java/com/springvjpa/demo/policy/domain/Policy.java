package com.springvjpa.demo.policy.domain;

import com.springvjpa.demo.common.ValidationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "policies")
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private LocalDate publicationDate;

    @Column(name = "policy_year", nullable = false)
    private Integer year;

    @Column(nullable = false, length = 1000)
    private String sourceUrl;

    @Column(length = 2000)
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ValidationStatus validationStatus;

    protected Policy() {
    }

    public Policy(String title, String state, String department, LocalDate publicationDate, Integer year, String sourceUrl,
                  String summary, ValidationStatus validationStatus) {
        this.title = title;
        this.state = state;
        this.department = department;
        this.publicationDate = publicationDate;
        this.year = year;
        this.sourceUrl = sourceUrl;
        this.summary = summary;
        this.validationStatus = validationStatus;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getState() {
        return state;
    }

    public String getDepartment() {
        return department;
    }

    public LocalDate getPublicationDate() {
        return publicationDate;
    }

    public Integer getYear() {
        return year;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getSummary() {
        return summary;
    }

    public ValidationStatus getValidationStatus() {
        return validationStatus;
    }
}
