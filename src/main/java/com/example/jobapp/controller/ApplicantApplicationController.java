package com.example.jobapp.controller;

import com.example.jobapp.dto.request.ApplyJobRequest;
import com.example.jobapp.dto.response.ApplicantApplicationResponse;
import com.example.jobapp.exception.ErrorResponse;
import com.example.jobapp.security.AuthenticatedUser;
import com.example.jobapp.service.ApplicantApplicationService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Applicant Applications", description = "APIs for applicants to apply and manage applications")
@SecurityRequirement(name = "bearerAuth")
public class ApplicantApplicationController {
    private final ApplicantApplicationService applicantApplicationService;

    public ApplicantApplicationController(ApplicantApplicationService applicantApplicationService) {
        this.applicantApplicationService = applicantApplicationService;
    }

    @PostMapping("/api/jobs/{jobId}/applications")
    @Operation(summary = "Apply for a job", description = "Ứng viên nộp hồ sơ ứng tuyển vào một công việc đang mở tuyển")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ứng tuyển thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ, job đã đóng hoặc đã nộp trước đó",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy Job",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApplicantApplicationResponse> applyJob (
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Integer jobId,
            @Valid @RequestBody ApplyJobRequest request
    ){
            ApplicantApplicationResponse response = applicantApplicationService.applyJob(
                    user.userId(),
                    jobId,
                    request
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/applications/me")
    @Operation(summary = "Get my applications", description = "Xem danh sách các đơn ứng tuyển của ứng viên đang đăng nhập")
    @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    public ResponseEntity<List<ApplicantApplicationResponse>> getMyApplications(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        List<ApplicantApplicationResponse> response = applicantApplicationService.getMyApplications(user.userId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/applications/{applicationId}")
    @Operation(summary = "Get application details", description = "Xem thông tin chi tiết của một đơn ứng tuyển của bản thân")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy chi tiết thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đơn ứng tuyển hoặc không có quyền xem",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApplicantApplicationResponse> getApplicationById(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "ID của đơn ứng tuyển")
            @PathVariable Integer applicationId
    ) {
        ApplicantApplicationResponse response = applicantApplicationService.getApplicationById(applicationId, user.userId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/applications/{applicationId}")
    @Operation(summary = "Withdraw an application", description = "Rút/hủy một đơn ứng tuyển của bản thân")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Rút đơn thành công (No Content)"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đơn ứng tuyển hoặc không có quyền rút",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> withdrawApplication(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "ID của đơn ứng tuyển cần rút")
            @PathVariable Integer applicationId
    ) {
        applicantApplicationService.withdrawApplication(user.userId(), applicationId);
        return ResponseEntity.noContent().build();
    }
}
