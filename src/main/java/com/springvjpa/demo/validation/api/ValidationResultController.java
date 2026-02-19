package com.springvjpa.demo.validation.api;

import com.springvjpa.demo.validation.service.ValidationResultService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/validation-results")
public class ValidationResultController {

    private final ValidationResultService validationResultService;

    public ValidationResultController(ValidationResultService validationResultService) {
        this.validationResultService = validationResultService;
    }

    @GetMapping
    public List<ValidationResultResponse> getValidationResults(@RequestParam(required = false) Long policyId) {
        return validationResultService.findByPolicy(policyId).stream()
                .map(ValidationResultResponse::from)
                .toList();
    }
}
