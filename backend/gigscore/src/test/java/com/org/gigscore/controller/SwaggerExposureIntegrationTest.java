package com.org.gigscore.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

class SwaggerExposureIntegrationTest {

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @ActiveProfiles("test")
    @TestPropertySource(properties = {
        "springdoc.swagger-ui.enabled=false",
        "springdoc.api-docs.enabled=false"
    })
    class WhenSwaggerDisabled {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void unauthenticatedRequests_areDeniedOrNotFound() throws Exception {
            mockMvc.perform(get("/v3/api-docs"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/swagger-ui/index.html"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @ActiveProfiles({"test", "dev"})
    @TestPropertySource(properties = {
        "springdoc.swagger-ui.enabled=true",
        "springdoc.api-docs.enabled=true"
    })
    class WhenDevProfileActive {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void unauthenticatedRequests_areAllowedInDev() throws Exception {
            mockMvc.perform(get("/v3/api-docs"))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/swagger-ui/index.html"))
                    .andExpect(status().isOk());
        }
    }
}
