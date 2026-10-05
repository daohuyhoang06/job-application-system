package com.example.jobapp.service;
import org.springframework.stereotype.Service;
import com.example.jobapp.repository.ApplicationRepository;
import com.example.jobapp.entity.Application;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import com.example.jobapp.dto.response.EmployerApplicationResponse;
import com.example.jobapp.security.AuthenticatedUser;
import com.example.jobapp.dto.request.EmployerStatusUpdateRequest;
@Service 
public class EmployerApplicationService {
    private final ApplicationRepository applicationRepository;

    public EmployerApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Transactional(readOnly = true)
    public List<EmployerApplicationResponse> getApplicationsForJob(AuthenticatedUser authenticatedUser, Integer jobId) {
        return applicationRepository.findByJob_IdAndJob_Employer_Id(jobId, authenticatedUser.userId()).stream().map(this::toResponse).toList();
    }
    private EmployerApplicationResponse toResponse(Application application) {
        return new EmployerApplicationResponse(
                application.getId(),
                application.getApplicant().getId(),
                application.getApplicant().getFullName(),
                application.getApplicant().getEmail(),
                application.getApplicant().getPhone(),
                application.getResumeUrl(),
                application.getCoverLetter(),
                application.getApplicant().getLocation(),
                application.getStatus(),
                application.getAppliedAt()
        );
    }
    @Transactional(readOnly = true)
    public EmployerApplicationResponse getApplicationById(AuthenticatedUser authenticatedUser, Integer applicationId) {
        Application application = applicationRepository.findByIdAndJob_Employer_Id(applicationId, authenticatedUser.userId())
                .orElseThrow(() -> new IllegalStateException("Application not found or you do not have permission to view it."));
        return toResponse(application);
    }
    @Transactional
    public EmployerApplicationResponse updateApplicationStatus(AuthenticatedUser authenticatedUser, Integer applicationId, EmployerStatusUpdateRequest request) {
        Application application = applicationRepository.findByIdAndJob_Employer_Id(applicationId, authenticatedUser.userId())
                .orElseThrow(() -> new IllegalStateException("Application not found or you do not have permission to update it."));
        application.setStatus(request.status());
        Application updatedApplication = applicationRepository.saveAndFlush(application);
        return toResponse(updatedApplication);
    }
}
