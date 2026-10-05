package com.example.jobapp.controller;

import com.example.jobapp.config.SecurityConfig;
import com.example.jobapp.dto.request.CreateJobRequest;
import com.example.jobapp.dto.response.EmployerJobResponse;
import com.example.jobapp.entity.enums.JobStatus;
import com.example.jobapp.entity.enums.PositionLevel;
import com.example.jobapp.entity.enums.UserRole;
import com.example.jobapp.exception.GlobalExceptionHandler;
import com.example.jobapp.security.AuthenticatedUser;
import com.example.jobapp.security.JwtAccessDeniedHandler;
import com.example.jobapp.security.JwtAuthenticationEntryPoint;
import com.example.jobapp.security.JwtAuthenticationFilter;
import com.example.jobapp.security.JwtService;
import com.example.jobapp.service.EmployerJobService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployerJobController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class,
        JwtAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
class EmployerJobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployerJobService employerJobService;

    @MockBean
    private JwtService jwtService;

    @BeforeEach
    void setUpAuthentication() {
        when(jwtService.parseAuthenticatedUser("employer-token"))
                .thenReturn(new AuthenticatedUser(7, UserRole.EMPLOYER));
        when(jwtService.parseAuthenticatedUser("applicant-token"))
                .thenReturn(new AuthenticatedUser(9, UserRole.APPLICANT));
    }

    @Test
    void employerEndpointWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/employer/jobs"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void applicantTokenReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/employer/jobs")
                        .header("Authorization", "Bearer applicant-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    void employerCanCreateJobAndPrincipalIdIsUsed() throws Exception {
        CreateJobRequest request = validCreateRequest();
        when(employerJobService.createJob(7, request)).thenReturn(jobResponse());

        mockMvc.perform(post("/api/employer/jobs")
                        .header("Authorization", "Bearer employer-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.employerId").value(7))
                .andExpect(jsonPath("$.status").value("OPEN"));

        verify(employerJobService).createJob(7, request);
    }

    @Test
    void invalidCreateRequestReturnsFieldErrors() throws Exception {
        CreateJobRequest request = new CreateJobRequest(
                "Software",
                " ",
                "Build backend services",
                "Ha Noi",
                PositionLevel.JUNIOR,
                15_000_000L,
                25_000_000L
        );

        mockMvc.perform(post("/api/employer/jobs")
                        .header("Authorization", "Bearer employer-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.title").exists());
    }

    @Test
    void deleteClosesJobAndReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/employer/jobs/11")
                        .header("Authorization", "Bearer employer-token"))
                .andExpect(status().isNoContent());

        verify(employerJobService).closeJob(7, 11);
    }

    private CreateJobRequest validCreateRequest() {
        return new CreateJobRequest(
                "Software",
                "Java Developer",
                "Build backend services",
                "Ha Noi",
                PositionLevel.JUNIOR,
                15_000_000L,
                25_000_000L
        );
    }

    private EmployerJobResponse jobResponse() {
        LocalDateTime now = LocalDateTime.of(2026, 10, 5, 9, 0);
        return new EmployerJobResponse(
                11,
                7,
                "Software",
                "Java Developer",
                "Build backend services",
                "Ha Noi",
                PositionLevel.JUNIOR,
                15_000_000L,
                25_000_000L,
                JobStatus.OPEN,
                now,
                now
        );
    }
}
