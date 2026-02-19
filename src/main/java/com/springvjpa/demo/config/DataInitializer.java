package com.springvjpa.demo.config;

import com.springvjpa.demo.common.ValidationStatus;
import com.springvjpa.demo.law.domain.LawReference;
import com.springvjpa.demo.law.repo.LawReferenceRepository;
import com.springvjpa.demo.policy.domain.Policy;
import com.springvjpa.demo.policy.repo.PolicyRepository;
import com.springvjpa.demo.validation.domain.ValidationResult;
import com.springvjpa.demo.validation.repo.ValidationResultRepository;
import java.time.LocalDate;
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
            ValidationResultRepository validationResultRepository
    ) {
        return args -> {
            if (policyRepository.count() > 0) {
                return;
            }

            Policy p1 = new Policy(
                    "State Urban Livelihood Support Policy",
                    "Karnataka",
                    "Urban Development",
                    LocalDate.of(2023, 2, 14),
                    2023,
                    "https://example.karnataka.gov.in/urban/policy-2023",
                    "Policy to improve skill support and self-employment for urban low-income households.",
                    ValidationStatus.VALID
            );
            Policy p2 = new Policy(
                    "Green Schools Energy Usage Notification",
                    "Karnataka",
                    "Education",
                    LocalDate.of(2024, 8, 2),
                    2024,
                    "https://example.karnataka.gov.in/edu/green-schools-2024",
                    "Guidelines for monitoring and reducing electricity consumption in public schools.",
                    ValidationStatus.NEEDS_REVIEW
            );
            Policy p3 = new Policy(
                    "Groundwater Commercial Extraction Circular",
                    "Karnataka",
                    "Water Resources",
                    LocalDate.of(2024, 5, 19),
                    2024,
                    "https://example.karnataka.gov.in/water/circular-05-2024",
                    "Temporary conditions for commercial groundwater extraction in notified zones.",
                    ValidationStatus.CONFLICT_SUSPECTED
            );
            policyRepository.saveAll(List.of(p1, p2, p3));

            LawReference l1 = new LawReference(
                    "Karnataka Ground Water Act",
                    "Section 7",
                    "Karnataka",
                    LocalDate.of(2011, 1, 1)
            );
            LawReference l2 = new LawReference(
                    "Energy Conservation Guidelines for Public Institutions",
                    "Rule 12",
                    "Karnataka",
                    LocalDate.of(2020, 4, 1)
            );
            lawReferenceRepository.saveAll(List.of(l1, l2));

            validationResultRepository.saveAll(List.of(
                    new ValidationResult(
                            p1,
                            l2,
                            "Valid",
                            "No conflicting clauses detected for energy usage or procurement references.",
                            LocalDate.now().minusDays(8)
                    ),
                    new ValidationResult(
                            p2,
                            l2,
                            "Needs Review",
                            "Implementation timeline is not aligned with mandatory annual reporting window.",
                            LocalDate.now().minusDays(2)
                    ),
                    new ValidationResult(
                            p3,
                            l1,
                            "Conflict Suspected",
                            "Circular grants temporary exemptions that may exceed statutory extraction caps.",
                            LocalDate.now().minusDays(1)
                    )
            ));
        };
    }
}
