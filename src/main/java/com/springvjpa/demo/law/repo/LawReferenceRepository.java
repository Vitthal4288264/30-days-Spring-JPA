package com.springvjpa.demo.law.repo;

import com.springvjpa.demo.law.domain.LawReference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LawReferenceRepository extends JpaRepository<LawReference, Long> {
}
