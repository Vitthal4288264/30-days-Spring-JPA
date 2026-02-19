package com.springvjpa.demo.policy.service;

import com.springvjpa.demo.common.ValidationStatus;
import com.springvjpa.demo.policy.api.YearlyPolicyCountResponse;
import com.springvjpa.demo.policy.domain.Policy;
import com.springvjpa.demo.policy.repo.PolicyRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PolicyService {

    private final PolicyRepository policyRepository;

    public PolicyService(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    public List<Policy> findPolicies(Integer year, String department, ValidationStatus status) {
        return policyRepository.findAll().stream()
                .filter(policy -> year == null || year.equals(policy.getYear()))
                .filter(policy -> department == null || department.equalsIgnoreCase(policy.getDepartment()))
                .filter(policy -> status == null || status == policy.getValidationStatus())
                .toList();
    }

    public List<YearlyPolicyCountResponse> yearlyCounts() {
        Map<Integer, Long> countByYear = policyRepository.findAll().stream()
                .collect(Collectors.groupingBy(Policy::getYear, Collectors.counting()));

        return countByYear.entrySet().stream()
                .map(entry -> new YearlyPolicyCountResponse(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(YearlyPolicyCountResponse::year))
                .toList();
    }
}
