package com.example.jobapp.controller;

import com.example.jobapp.dto.request.CreateJobRequest;
import com.example.jobapp.dto.request.UpdateJobRequest;
import com.example.jobapp.dto.response.EmployerJobResponse;
import com.example.jobapp.dto.response.PageResponse;
import com.example.jobapp.exception.ErrorResponse;
import com.example.jobapp.security.AuthenticatedUser;
import com.example.jobapp.service.EmployerJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employer/jobs")
@Tag(name = "Employer Jobs", description = "Manage jobs owned by the authenticated employer")
@SecurityRequirement(name = "bearerAuth")
public class EmployerJobController {

    private final EmployerJobService employerJobService;

    public EmployerJobController(EmployerJobService employerJobService) {
        this.employerJobService = employerJobService;
    }

    @PostMapping
    @Operation(summary = "Create a job")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Job created"),
            @ApiResponse(responseCode = "400", description = "Invalid job data", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Employer role required", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EmployerJobResponse> createJob(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateJobRequest request
    ) {
        EmployerJobResponse response = employerJobService.createJob(user.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List jobs owned by the current employer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Jobs returned"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Employer role required", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PageResponse<EmployerJobResponse>> getJobs(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "Zero-based page index", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size from 1 to 100", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(employerJobService.getJobs(user.userId(), page, size));
    }

    @GetMapping("/{jobId}")
    @Operation(summary = "Get an owned job")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Employer role required", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Job not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EmployerJobResponse> getJob(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Integer jobId
    ) {
        return ResponseEntity.ok(employerJobService.getJob(user.userId(), jobId));
    }

    @PatchMapping("/{jobId}")
    @Operation(summary = "Update an owned job")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job updated"),
            @ApiResponse(responseCode = "400", description = "Invalid job data", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Employer role required", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Job not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EmployerJobResponse> updateJob(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Integer jobId,
            @Valid @RequestBody UpdateJobRequest request
    ) {
        return ResponseEntity.ok(employerJobService.updateJob(user.userId(), jobId, request));
    }

    @DeleteMapping("/{jobId}")
    @Operation(summary = "Close an owned job without deleting its applications")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Job closed"),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Employer role required", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Job not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> closeJob(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Integer jobId
    ) {
        employerJobService.closeJob(user.userId(), jobId);
        return ResponseEntity.noContent().build();
    }
}
