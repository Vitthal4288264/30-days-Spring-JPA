package com.springvjpa.demo.validation.service;

import com.springvjpa.demo.validation.domain.ValidationResult;
import com.springvjpa.demo.validation.repo.ValidationResultRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ValidationResultService {

    private final ValidationResultRepository validationResultRepository;

    public ValidationResultService(ValidationResultRepository validationResultRepository) {
        this.validationResultRepository = validationResultRepository;
    }

    public List<ValidationResult> findByPolicy(Long policyId) {
        if (policyId == null) {
            return validationResultRepository.findAll();
        }
        return validationResultRepository.findByPolicyId(policyId);
    }
}
