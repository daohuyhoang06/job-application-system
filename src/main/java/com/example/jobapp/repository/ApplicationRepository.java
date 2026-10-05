package com.example.jobapp.repository;
import com.example.jobapp.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface ApplicationRepository extends JpaRepository<Application, Integer> {
    List<Application> findByJob_IdAndJob_Employer_Id(Integer jobId, Integer employerId);
    Optional<Application> findByIdAndJob_Employer_Id(Integer applicationId, Integer employerId);
}