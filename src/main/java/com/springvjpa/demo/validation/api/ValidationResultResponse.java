package com.springvjpa.demo.validation.api;

import com.springvjpa.demo.validation.domain.ValidationResult;
import java.time.LocalDate;

public record ValidationResultResponse(
        Long id,
        Long policyId,
        String policyTitle,
        Long lawReferenceId,
        String lawName,
        String result,
        String remarks,
        LocalDate checkedOn
) {
    public static ValidationResultResponse from(ValidationResult validationResult) {
        return new ValidationResultResponse(
                validationResult.getId(),
                validationResult.getPolicy().getId(),
                validationResult.getPolicy().getTitle(),
                validationResult.getLawReference().getId(),
                validationResult.getLawReference().getLawName(),
                validationResult.getResult(),
                validationResult.getRemarks(),
                validationResult.getCheckedOn()
        );
    }
}
