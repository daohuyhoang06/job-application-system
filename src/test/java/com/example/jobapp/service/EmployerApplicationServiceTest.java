package com.example.jobapp.service;

import com.example.jobapp.dto.request.EmployerStatusUpdateRequest;
import com.example.jobapp.dto.response.EmployerApplicationResponse;
import com.example.jobapp.entity.Application;
import com.example.jobapp.entity.Applicant;
import com.example.jobapp.entity.enums.ApplicationStatus;
import com.example.jobapp.entity.enums.UserRole;
import com.example.jobapp.repository.ApplicationRepository;
import com.example.jobapp.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployerApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private EmployerApplicationService employerApplicationService;

    @Test
    void getApplicationsForJobReturnsApplicationsOwnedByEmployer() {
        Application application = application();
        when(applicationRepository.findByJob_IdAndJob_Employer_Id(5, 10))
                .thenReturn(List.of(application));

        List<EmployerApplicationResponse> response =
                employerApplicationService.getApplicationsForJob(
                        new AuthenticatedUser(10, UserRole.EMPLOYER),
                        5
                );

        assertThat(response).hasSize(1);
        assertThat(response.get(0).applicationId()).isEqualTo(20);
        assertThat(response.get(0).applicantId()).isEqualTo(1);
        assertThat(response.get(0).applicantFullName())
                .isEqualTo("Nguyen Van A");
        assertThat(response.get(0).applicationStatus())
                .isEqualTo(ApplicationStatus.PENDING);
    }

    @Test
    void getApplicationByIdReturnsApplicationOwnedByEmployer() {
        Application application = application();
        when(applicationRepository.findByIdAndJob_Employer_Id(20, 10))
                .thenReturn(Optional.of(application));

        EmployerApplicationResponse response =
                employerApplicationService.getApplicationById(
                        new AuthenticatedUser(10, UserRole.EMPLOYER),
                        20
                );

        assertThat(response.applicationId()).isEqualTo(20);
        assertThat(response.applicantEmail())
                .isEqualTo("applicant@example.com");
    }

    @Test
    void updateApplicationStatusChangesStatus() {
        Application application = application();
        when(applicationRepository.findByIdAndJob_Employer_Id(20, 10))
                .thenReturn(Optional.of(application));
        when(applicationRepository.saveAndFlush(application))
                .thenReturn(application);

        EmployerApplicationResponse response =
                employerApplicationService.updateApplicationStatus(
                        new AuthenticatedUser(10, UserRole.EMPLOYER),
                        20,
                        new EmployerStatusUpdateRequest(ApplicationStatus.INTERVIEW)
                );

        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.INTERVIEW);
        assertThat(response.applicationStatus())
                .isEqualTo(ApplicationStatus.INTERVIEW);
    }

    @Test
    void getApplicationByIdRejectsApplicationOwnedByAnotherEmployer() {
        when(applicationRepository.findByIdAndJob_Employer_Id(20, 99))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> employerApplicationService.getApplicationById(
                new AuthenticatedUser(99, UserRole.EMPLOYER),
                20
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("permission");
    }

    private Application application() {
        Applicant applicant = new Applicant();
        applicant.setId(1);
        applicant.setEmail("applicant@example.com");
        applicant.setFullName("Nguyen Van A");
        applicant.setPhone("0912345678");
        applicant.setLocation("Ha Noi");

        Application application = new Application();
        application.setId(20);
        application.setApplicant(applicant);
        application.setResumeUrl("https://example.com/resume.pdf");
        application.setCoverLetter("I am interested in this position.");
        application.setStatus(ApplicationStatus.PENDING);
        return application;
    }
}
