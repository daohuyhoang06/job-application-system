package com.example.jobapp.controller;
import com.example.jobapp.dto.response.ApplicantProfileResponse;
import com.example.jobapp.security.AuthenticatedUser;
import com.example.jobapp.service.ApplicantService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.example.jobapp.dto.request.ApplicantProfileUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController 
@RequestMapping("/api/applicants")
public class ApplicantController {
    private final ApplicantService applicantService;

    public ApplicantController(ApplicantService applicantService) {
        this.applicantService = applicantService;
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me") 
    public ResponseEntity<ApplicantProfileResponse> getMyProfile(Authentication authentication) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        ApplicantProfileResponse profileResponse = applicantService.getMyProfile(authenticatedUser);
        return ResponseEntity.ok(profileResponse);
    }
    
    @PatchMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApplicantProfileResponse> updateMyProfile(Authentication authentication, @Valid @RequestBody ApplicantProfileUpdateRequest request) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        ApplicantProfileResponse updatedProfileResponse = applicantService.updateMyProfile(authenticatedUser, request);
        return ResponseEntity.ok(updatedProfileResponse);
    }
}
