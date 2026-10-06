package com.example.jobapp.repository.custom.impl;

import com.example.jobapp.entity.Job;
import com.example.jobapp.entity.enums.JobStatus;
import com.example.jobapp.entity.enums.PositionLevel;
import com.example.jobapp.repository.custom.JobRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.List;

public class JobRepositoryCustomImpl implements JobRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Job> searchPublicJobs(
            JobStatus status,
            String keyword,
            String location,
            String category,
            PositionLevel level,
            Long salaryMin,
            Pageable pageable
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Job> cq = cb.createQuery(Job.class);
        Root<Job> job = cq.from(Job.class);
        List<Predicate> predicates = buildPredicates(cb, job, status, keyword, location, category, level, salaryMin);
        cq.where(cb.and(predicates.toArray(new Predicate[0])));
        cq.orderBy(cb.desc(job.get("createdAt")));
        TypedQuery<Job> query = entityManager.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Job> jobs = query.getResultList();

        CriteriaQuery<Long> countCq = cb.createQuery(Long.class);
        Root<Job> countRoot = countCq.from(Job.class);
        List<Predicate> countPredicates = buildPredicates(cb, countRoot, status, keyword, location, category, level, salaryMin);
        countCq.select(cb.count(countRoot)).where(cb.and(countPredicates.toArray(new Predicate[0])));
        Long total = entityManager.createQuery(countCq).getSingleResult();
        return new PageImpl<>(jobs, pageable, total);
    }
    private List<Predicate> buildPredicates(
            CriteriaBuilder cb,
            Root<Job> job,
            JobStatus status,
            String keyword,
            String location,
            String category,
            PositionLevel level,
            Long salaryMin
    ) {
        List<Predicate> predicates = new ArrayList<>();
        // Luôn lọc status (thường là OPEN)
        if (status != null) {
            predicates.add(cb.equal(job.get("status"), status));
        }
        // Lọc từ khóa theo title HOẶC description
        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.toLowerCase().trim() + "%";
            Predicate titleMatch = cb.like(cb.lower(job.get("title")), pattern);
            Predicate descMatch = cb.like(cb.lower(job.get("description")), pattern);
            predicates.add(cb.or(titleMatch, descMatch));
        }
        // Lọc location
        if (location != null && !location.isBlank()) {
            predicates.add(cb.like(cb.lower(job.get("location")), "%" + location.toLowerCase().trim() + "%"));
        }
        // Lọc category
        if (category != null && !category.isBlank()) {
            predicates.add(cb.equal(cb.lower(job.get("category")), category.toLowerCase().trim()));
        }
        // Lọc position level
        if (level != null) {
            predicates.add(cb.equal(job.get("positionLevel"), level));
        }
        // Lọc mức lương
        if (salaryMin != null) {
            Predicate maxSalaryOk = cb.greaterThanOrEqualTo(job.get("salaryMax"), salaryMin);
            Predicate minSalaryOk = cb.and(cb.isNull(job.get("salaryMax")), cb.greaterThanOrEqualTo(job.get("salaryMin"), salaryMin));
            predicates.add(cb.or(maxSalaryOk, minSalaryOk));
        }
        return predicates;
    }
}
