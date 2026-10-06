package com.example.jobapp.repository.custom;

import com.example.jobapp.entity.Job;
import com.example.jobapp.entity.enums.JobStatus;
import com.example.jobapp.entity.enums.PositionLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JobRepositoryCustom {
    Page<Job> searchPublicJobs(
            JobStatus status,
            String keyword,
            String location,
            String category,
            PositionLevel level,
            Long salaryMin,
            Pageable pageable
    );
}