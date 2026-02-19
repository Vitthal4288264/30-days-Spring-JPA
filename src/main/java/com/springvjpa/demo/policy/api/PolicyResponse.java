package com.springvjpa.demo.policy.api;

import com.springvjpa.demo.common.ValidationStatus;
import com.springvjpa.demo.policy.domain.Policy;
import java.time.LocalDate;

public record PolicyResponse(
        Long id,
        String title,
        String state,
        String department,
        LocalDate publicationDate,
        Integer year,
        String sourceUrl,
        String summary,
        ValidationStatus validationStatus
) {
    public static PolicyResponse from(Policy policy) {
        return new PolicyResponse(
                policy.getId(),
                policy.getTitle(),
                policy.getState(),
                policy.getDepartment(),
                policy.getPublicationDate(),
                policy.getYear(),
                policy.getSourceUrl(),
                policy.getSummary(),
                policy.getValidationStatus()
        );
    }
}
