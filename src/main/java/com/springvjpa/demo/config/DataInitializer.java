package com.springvjpa.demo.config;

import com.springvjpa.demo.common.ValidationStatus;
import com.springvjpa.demo.law.domain.LawReference;
import com.springvjpa.demo.law.repo.LawReferenceRepository;
import com.springvjpa.demo.policy.domain.Policy;
import com.springvjpa.demo.policy.repo.PolicyRepository;
import com.springvjpa.demo.validation.domain.ValidationResult;
import com.springvjpa.demo.validation.repo.ValidationResultRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner loadData(
            PolicyRepository policyRepository,
            LawReferenceRepository lawReferenceRepository,
            ValidationResultRepository validationResultRepository,
            DepartmentScraperClient departmentScraperClient
    ) {
        return args -> {
            if (policyRepository.count() > 0) {
                return;
            }

            List<DepartmentScraperClient.ScrapedDepartment> scrapedDepartments =
                    departmentScraperClient.fetchTargetDepartments();

            List<Policy> policies = new ArrayList<>();
            LocalDate publicationDate = LocalDate.now();

            for (DepartmentScraperClient.ScrapedDepartment department : scrapedDepartments) {
                ValidationStatus status = department.name().toLowerCase().contains("revenue")
                        ? ValidationStatus.NEEDS_REVIEW
                        : ValidationStatus.VALID;

                policies.add(new Policy(
                        department.name() + " - Latest Department Listing",
                        "Karnataka",
                        department.name(),
                        publicationDate,
                        publicationDate.getYear(),
                        department.sourceUrl(),
                        "Auto-scraped from Karnataka Mahiti Kanaja department portal.",
                        status
                ));
            }

            policyRepository.saveAll(policies);

            LawReference revenueLaw = new LawReference(
                    "Karnataka Land Revenue Act",
                    "Section 1",
                    "Karnataka",
                    LocalDate.of(1964, 1, 1)
            );
            LawReference urbanLaw = new LawReference(
                    "Karnataka Town and Country Planning Act",
                    "Section 14",
                    "Karnataka",
                    LocalDate.of(1961, 1, 1)
            );
            lawReferenceRepository.saveAll(List.of(revenueLaw, urbanLaw));

            List<ValidationResult> validationResults = new ArrayList<>();
            for (Policy policy : policies) {
                LawReference lawReference = policy.getDepartment().toLowerCase().contains("revenue")
                        ? revenueLaw
                        : urbanLaw;
                String result = policy.getValidationStatus() == ValidationStatus.NEEDS_REVIEW
                        ? "Needs Review"
                        : "Valid";

                validationResults.add(new ValidationResult(
                        policy,
                        lawReference,
                        result,
                        "Validation generated for scraped department information.",
                        publicationDate
                ));
            }

            validationResultRepository.saveAll(validationResults);
        };
    }
}
