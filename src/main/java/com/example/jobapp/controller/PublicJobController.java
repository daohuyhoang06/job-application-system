package com.example.jobapp.controller;

import com.example.jobapp.dto.response.PageResponse;
import com.example.jobapp.dto.response.PublicJobResponse;
import com.example.jobapp.entity.enums.PositionLevel;
import com.example.jobapp.exception.ErrorResponse;
import com.example.jobapp.service.PublicJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Public Jobs", description = "Public endpoints to browse and search active jobs")
public class PublicJobController {

    private final PublicJobService jobService;

    public PublicJobController(PublicJobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    @Operation(summary = "Search and list public jobs", description = "Lấy danh sách công việc đang tuyển (OPEN) có hỗ trợ tìm kiếm và phân trang")
    @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    public ResponseEntity<PageResponse<PublicJobResponse>> getJobs(
            @Parameter(description = "Từ khóa tìm trong tiêu đề hoặc mô tả")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "Địa điểm làm việc")
            @RequestParam(required = false) String location,

            @Parameter(description = "Danh mục/chuyên ngành công việc")
            @RequestParam(required = false) String category,

            @Parameter(description = "Cấp bậc: INTERN, FRESHER, JUNIOR, SENIOR")
            @RequestParam(required = false) PositionLevel level,

            @Parameter(description = "Mức lương tối thiểu mong muốn")
            @RequestParam(required = false) Long salaryMin,

            @Parameter(description = "Số thứ tự trang (bắt đầu từ 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Số lượng tin mỗi trang (mặc định 10)")
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<PublicJobResponse> response = jobService.getPublicJobs(
                keyword, location, category, level, salaryMin, page, size
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{jobId}")
    @Operation(summary = "Get job details", description = "Xem thông tin chi tiết của một công việc đang tuyển")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy công việc hoặc công việc đã đóng",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PublicJobResponse> getJobById(
            @Parameter(description = "ID của công việc cần xem")
            @PathVariable Integer jobId
    ) {
        PublicJobResponse response = jobService.getPublicJobById(jobId);
        return ResponseEntity.ok(response);
    }
}