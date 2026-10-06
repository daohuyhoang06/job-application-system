package com.example.jobapp.repository;

import com.example.jobapp.entity.Job;
import com.example.jobapp.entity.enums.JobStatus;
import com.example.jobapp.repository.custom.JobRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Integer>, JobRepositoryCustom {

    Page<Job> findAllByEmployerId(Integer employerId, Pageable pageable);

    Optional<Job> findByIdAndEmployerId(Integer jobId, Integer employerId);

    Optional<Job> findByIdAndStatus(Integer id, JobStatus status);
}
