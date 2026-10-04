package com.example.jobapp.repository;

import com.example.jobapp.entity.Employer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployerRepository extends JpaRepository<Employer, Integer> {

    Optional<Employer> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
