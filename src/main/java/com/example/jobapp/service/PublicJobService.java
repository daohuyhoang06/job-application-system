package com.example.jobapp.service;

import com.example.jobapp.dto.response.CompanySummaryResponse;
import com.example.jobapp.dto.response.PageResponse;
import com.example.jobapp.dto.response.PublicJobResponse;
import com.example.jobapp.entity.Company;
import com.example.jobapp.entity.Job;
import com.example.jobapp.entity.enums.JobStatus;
import com.example.jobapp.entity.enums.PositionLevel;
import com.example.jobapp.exception.ResourceNotFoundException;
import com.example.jobapp.repository.JobRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicJobService {

    private final JobRepository jobRepository;

    public PublicJobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<PublicJobResponse> getPublicJobs(
            String keyword,
            String location,
            String category,
            PositionLevel level,
            Long salaryMin,
            int page,
            int size
    ) {
        int validPage = Math.max(0, page);
        int validSize = Math.min(Math.max(1, size), 20);

        PageRequest pageRequest = PageRequest.of(
                validPage,
                validSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        // Chuẩn hóa chuỗi rỗng thành null
        String cleanKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String cleanLocation = (location != null && !location.isBlank()) ? location.trim() : null;
        String cleanCategory = (category != null && !category.isBlank()) ? category.trim() : null;

        Page<PublicJobResponse> jobPage = jobRepository.searchPublicJobs(
                JobStatus.OPEN,
                cleanKeyword,
                cleanLocation,
                cleanCategory,
                level,
                salaryMin,
                pageRequest
        ).map(this::toResponse);

        return new PageResponse<>(
                jobPage.getContent(),
                jobPage.getNumber(),
                jobPage.getSize(),
                jobPage.getTotalElements(),
                jobPage.getTotalPages(),
                jobPage.isFirst(),
                jobPage.isLast()
        );
    }

    @Transactional(readOnly = true)
    public PublicJobResponse getPublicJobById(Integer jobId) {
        Job job = jobRepository.findByIdAndStatus(jobId, JobStatus.OPEN)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        return toResponse(job);
    }

    private PublicJobResponse toResponse(Job job) {
        Company company = job.getEmployer().getCompany();
        CompanySummaryResponse companySummary = new CompanySummaryResponse(
                company.getId(),
                company.getName(),
                company.getDescription()
        );

        return new PublicJobResponse(
                job.getId(),
                job.getTitle(),
                job.getCategory(),
                job.getDescription(),
                job.getLocation(),
                job.getPositionLevel(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getStatus(),
                companySummary,
                job.getCreatedAt(),
                job.getUpdatedAt()
        );
    }
}