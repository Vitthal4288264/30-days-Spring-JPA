package com.springvjpa.demo.law.api;

import com.springvjpa.demo.law.repo.LawReferenceRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/law-references")
public class LawReferenceController {

    private final LawReferenceRepository lawReferenceRepository;

    public LawReferenceController(LawReferenceRepository lawReferenceRepository) {
        this.lawReferenceRepository = lawReferenceRepository;
    }

    @GetMapping
    public List<LawReferenceResponse> list() {
        return lawReferenceRepository.findAll().stream()
                .map(LawReferenceResponse::from)
                .toList();
    }
}
