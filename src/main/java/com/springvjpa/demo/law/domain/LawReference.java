package com.springvjpa.demo.law.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "law_references")
public class LawReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String lawName;

    @Column(nullable = false)
    private String section;

    @Column(nullable = false)
    private String jurisdiction;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    protected LawReference() {
    }

    public LawReference(String lawName, String section, String jurisdiction, LocalDate effectiveDate) {
        this.lawName = lawName;
        this.section = section;
        this.jurisdiction = jurisdiction;
        this.effectiveDate = effectiveDate;
    }

    public Long getId() {
        return id;
    }

    public String getLawName() {
        return lawName;
    }

    public String getSection() {
        return section;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }
}
