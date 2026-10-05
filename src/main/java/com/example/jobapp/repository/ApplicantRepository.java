package com.example.jobapp.repository;

import com.example.jobapp.entity.Applicant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicantRepository extends JpaRepository<Applicant, Integer> {

    Optional<Applicant> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
