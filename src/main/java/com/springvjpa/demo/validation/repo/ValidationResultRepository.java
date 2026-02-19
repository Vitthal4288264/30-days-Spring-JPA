package com.springvjpa.demo.validation.repo;

import com.springvjpa.demo.validation.domain.ValidationResult;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValidationResultRepository extends JpaRepository<ValidationResult, Long> {

    List<ValidationResult> findByPolicyId(Long policyId);
}
