package com.springvjpa.demo.policy.repo;

import com.springvjpa.demo.common.ValidationStatus;
import com.springvjpa.demo.policy.domain.Policy;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyRepository extends JpaRepository<Policy, Long> {

    List<Policy> findByYear(Integer year);

    List<Policy> findByDepartmentIgnoreCase(String department);

    List<Policy> findByValidationStatus(ValidationStatus validationStatus);

    List<Policy> findByYearAndDepartmentIgnoreCase(Integer year, String department);
}
