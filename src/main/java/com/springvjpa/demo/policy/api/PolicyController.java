package com.springvjpa.demo.policy.api;

import com.springvjpa.demo.common.ValidationStatus;
import com.springvjpa.demo.policy.service.PolicyService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @GetMapping
    public List<PolicyResponse> getPolicies(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) ValidationStatus status
    ) {
        return policyService.findPolicies(year, department, status).stream()
                .map(PolicyResponse::from)
                .toList();
    }

    @GetMapping("/yearly-count")
    public List<YearlyPolicyCountResponse> getYearlyCount() {
        return policyService.yearlyCounts();
    }
}
