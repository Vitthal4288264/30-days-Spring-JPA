package com.springvjpa.demo.law.api;

import com.springvjpa.demo.law.domain.LawReference;
import java.time.LocalDate;

public record LawReferenceResponse(
        Long id,
        String lawName,
        String section,
        String jurisdiction,
        LocalDate effectiveDate
) {
    public static LawReferenceResponse from(LawReference reference) {
        return new LawReferenceResponse(
                reference.getId(),
                reference.getLawName(),
                reference.getSection(),
                reference.getJurisdiction(),
                reference.getEffectiveDate()
        );
    }
}
