package com.example.jobapp.controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import com.example.jobapp.service.EmployerApplicationService;
import com.example.jobapp.dto.response.EmployerApplicationResponse;
import com.example.jobapp.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import java.util.List;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import com.example.jobapp.dto.request.EmployerStatusUpdateRequest;

@RestController
@RequestMapping("/api/employer") 
@SecurityRequirement(name = "bearerAuth")
public class EmployerController {
    private final EmployerApplicationService employerApplicationService;
    public EmployerController(EmployerApplicationService employerApplicationService) {
        this.employerApplicationService = employerApplicationService;
    }
    @GetMapping("/jobs/{jobId}/applications")
    public ResponseEntity<List<EmployerApplicationResponse>> getApplicationsForJob(@PathVariable Integer jobId, Authentication authentication) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        List<EmployerApplicationResponse> applications = employerApplicationService.getApplicationsForJob(authenticatedUser, jobId);
        return ResponseEntity.ok(applications);
    }
    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<EmployerApplicationResponse> getApplicationById(@PathVariable Integer applicationId, Authentication authentication) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        EmployerApplicationResponse applicationResponse = employerApplicationService.getApplicationById(authenticatedUser, applicationId);
        return ResponseEntity.ok(applicationResponse);
    }
    @PatchMapping("/applications/{applicationId}/status")
    public ResponseEntity<EmployerApplicationResponse> updateApplicationStatus(@PathVariable Integer applicationId, @Valid @RequestBody EmployerStatusUpdateRequest request, Authentication authentication) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        EmployerApplicationResponse applicationResponse = employerApplicationService.updateApplicationStatus(authenticatedUser, applicationId, request);
        return ResponseEntity.ok(applicationResponse);
    }
}
