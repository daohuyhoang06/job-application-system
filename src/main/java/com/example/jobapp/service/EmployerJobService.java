package com.example.jobapp.service;

import com.example.jobapp.dto.request.CreateJobRequest;
import com.example.jobapp.dto.request.UpdateJobRequest;
import com.example.jobapp.dto.response.EmployerJobResponse;
import com.example.jobapp.dto.response.PageResponse;
import com.example.jobapp.entity.Employer;
import com.example.jobapp.entity.Job;
import com.example.jobapp.entity.enums.JobStatus;
import com.example.jobapp.exception.InvalidJobDataException;
import com.example.jobapp.exception.ResourceNotFoundException;
import com.example.jobapp.repository.EmployerRepository;
import com.example.jobapp.repository.JobRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployerJobService {

    private static final int MAX_PAGE_SIZE = 100;

    private final JobRepository jobRepository;
    private final EmployerRepository employerRepository;

    public EmployerJobService(
            JobRepository jobRepository,
            EmployerRepository employerRepository
    ) {
        this.jobRepository = jobRepository;
        this.employerRepository = employerRepository;
    }

    @Transactional
    public EmployerJobResponse createJob(Integer employerId, CreateJobRequest request) {
        validateSalaryRange(request.salaryMin(), request.salaryMax());

        Employer employer = employerRepository.findById(employerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found"));

        Job job = new Job();
        job.setEmployer(employer);
        job.setCategory(normalize(request.category()));
        job.setTitle(normalize(request.title()));
        job.setDescription(normalize(request.description()));
        job.setLocation(normalize(request.location()));
        job.setPositionLevel(request.positionLevel());
        job.setSalaryMin(request.salaryMin());
        job.setSalaryMax(request.salaryMax());
        job.setStatus(JobStatus.OPEN);

        return toResponse(jobRepository.saveAndFlush(job));
    }

    @Transactional(readOnly = true)
    public PageResponse<EmployerJobResponse> getJobs(Integer employerId, int page, int size) {
        validatePagination(page, size);

        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        Page<EmployerJobResponse> jobs = jobRepository
                .findAllByEmployerId(employerId, pageRequest)
                .map(this::toResponse);

        return new PageResponse<>(
                jobs.getContent(),
                jobs.getNumber(),
                jobs.getSize(),
                jobs.getTotalElements(),
                jobs.getTotalPages(),
                jobs.isFirst(),
                jobs.isLast()
        );
    }

    @Transactional(readOnly = true)
    public EmployerJobResponse getJob(Integer employerId, Integer jobId) {
        return toResponse(findOwnedJob(employerId, jobId));
    }

    @Transactional
    public EmployerJobResponse updateJob(
            Integer employerId,
            Integer jobId,
            UpdateJobRequest request
    ) {
        if (!request.hasChanges()) {
            throw new InvalidJobDataException("At least one field must be provided for update");
        }

        Job job = findOwnedJob(employerId, jobId);

        if (request.category() != null) {
            job.setCategory(normalize(request.category()));
        }
        if (request.title() != null) {
            job.setTitle(normalize(request.title()));
        }
        if (request.description() != null) {
            job.setDescription(normalize(request.description()));
        }
        if (request.location() != null) {
            job.setLocation(normalize(request.location()));
        }
        if (request.positionLevel() != null) {
            job.setPositionLevel(request.positionLevel());
        }
        if (request.salaryMin() != null) {
            job.setSalaryMin(request.salaryMin());
        }
        if (request.salaryMax() != null) {
            job.setSalaryMax(request.salaryMax());
        }
        if (request.status() != null) {
            job.setStatus(request.status());
        }

        validateSalaryRange(job.getSalaryMin(), job.getSalaryMax());
        return toResponse(jobRepository.saveAndFlush(job));
    }

    @Transactional
    public void closeJob(Integer employerId, Integer jobId) {
        Job job = findOwnedJob(employerId, jobId);
        if (job.getStatus() != JobStatus.CLOSED) {
            job.setStatus(JobStatus.CLOSED);
            jobRepository.saveAndFlush(job);
        }
    }

    private Job findOwnedJob(Integer employerId, Integer jobId) {
        return jobRepository.findByIdAndEmployerId(jobId, employerId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new InvalidJobDataException("Page must not be negative");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidJobDataException("Size must be between 1 and 100");
        }
    }

    private void validateSalaryRange(Long salaryMin, Long salaryMax) {
        if (salaryMin != null && salaryMin < 0) {
            throw new InvalidJobDataException("Minimum salary must not be negative");
        }
        if (salaryMax != null && salaryMax < 0) {
            throw new InvalidJobDataException("Maximum salary must not be negative");
        }
        if (salaryMin != null && salaryMax != null && salaryMax < salaryMin) {
            throw new InvalidJobDataException(
                    "Maximum salary must be greater than or equal to minimum salary"
            );
        }
    }

    private String normalize(String value) {
        return value.trim();
    }

    private EmployerJobResponse toResponse(Job job) {
        return new EmployerJobResponse(
                job.getId(),
                job.getEmployer().getId(),
                job.getCategory(),
                job.getTitle(),
                job.getDescription(),
                job.getLocation(),
                job.getPositionLevel(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getStatus(),
                job.getCreatedAt(),
                job.getUpdatedAt()
        );
    }
}
