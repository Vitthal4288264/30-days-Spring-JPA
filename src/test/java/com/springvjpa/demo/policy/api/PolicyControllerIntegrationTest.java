package com.springvjpa.demo.policy.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PolicyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnPoliciesFilteredByYearAndStatus() throws Exception {
        mockMvc.perform(get("/api/policies")
                        .queryParam("year", "2024")
                        .queryParam("status", "NEEDS_REVIEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].department").value("Education"))
                .andExpect(jsonPath("$[0].validationStatus").value("NEEDS_REVIEW"));
    }

    @Test
    void shouldReturnYearlyCounts() throws Exception {
        mockMvc.perform(get("/api/policies/yearly-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].year").exists())
                .andExpect(jsonPath("$[0].count").exists());
    }
}
