package com.example.jobapp.controller;

import com.example.jobapp.dto.request.ApplicantRegisterRequest;
import com.example.jobapp.dto.request.EmployerRegisterRequest;
import com.example.jobapp.dto.request.LoginRequest;
import com.example.jobapp.dto.response.ApplicantRegisterResponse;
import com.example.jobapp.dto.response.AuthResponse;
import com.example.jobapp.dto.response.EmployerRegisterResponse;
import com.example.jobapp.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Applicant and employer authentication")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/applicants/register")
    @Operation(summary = "Register an applicant")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Applicant registered"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "Email already exists", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ApplicantRegisterResponse> registerApplicant(
            @Valid @RequestBody ApplicantRegisterRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerApplicant(request));
    }

    @PostMapping("/applicants/login")
    @Operation(summary = "Login as an applicant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    public ResponseEntity<AuthResponse> loginApplicant(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.loginApplicant(request));
    }

    @PostMapping("/employers/register")
    @Operation(summary = "Register an employer and company")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Employer registered"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<EmployerRegisterResponse> registerEmployer(
            @Valid @RequestBody EmployerRegisterRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerEmployer(request));
    }

    @PostMapping("/employers/login")
    @Operation(summary = "Login as an employer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    public ResponseEntity<AuthResponse> loginEmployer(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.loginEmployer(request));
    }
}
