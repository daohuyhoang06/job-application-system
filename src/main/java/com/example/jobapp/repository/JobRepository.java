package com.example.jobapp.repository;

import com.example.jobapp.entity.Job;
import com.example.jobapp.entity.enums.JobStatus;
import com.example.jobapp.entity.enums.PositionLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Integer> {

    Page<Job> findAllByEmployerId(Integer employerId, Pageable pageable);

    Optional<Job> findByIdAndEmployerId(Integer jobId, Integer employerId);

    Optional<Job> findByIdAndStatus(Integer id, JobStatus status);

    @Query("""
        SELECT j FROM Job j
        WHERE (:status IS NULL OR j.status = :status)
          AND (:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                                OR LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%')))
          AND (:category IS NULL OR LOWER(j.category) = LOWER(:category))
          AND (:level IS NULL OR j.positionLevel = :level)
          AND (:salaryMin IS NULL OR j.salaryMax >= :salaryMin
                                  OR (j.salaryMax IS NULL AND j.salaryMin >= :salaryMin))
    """)
    Page<Job> searchPublicJobs(
            @Param("status") JobStatus status,
            @Param("keyword") String keyword,
            @Param("location") String location,
            @Param("category") String category,
            @Param("level") PositionLevel level,
            @Param("salaryMin") Long salaryMin,
            Pageable pageable
    );
}
