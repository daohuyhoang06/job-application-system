package com.example.jobapp.repository;

import com.example.jobapp.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Integer> {

    Page<Job> findAllByEmployerId(Integer employerId, Pageable pageable);

    Optional<Job> findByIdAndEmployerId(Integer jobId, Integer employerId);
}
