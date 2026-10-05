package com.example.jobapp.service;

import com.example.jobapp.dto.request.ApplicantProfileUpdateRequest;
import com.example.jobapp.dto.response.ApplicantProfileResponse;
import com.example.jobapp.entity.Applicant;
import com.example.jobapp.entity.enums.UserRole;
import com.example.jobapp.repository.ApplicantRepository;
import com.example.jobapp.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicantServiceTest {

    @Mock
    private ApplicantRepository applicantRepository;

    @InjectMocks
    private ApplicantService applicantService;

    @Test
    void getMyProfileReturnsCurrentApplicantProfile() {
        Applicant applicant = applicant();
        when(applicantRepository.findById(1)).thenReturn(Optional.of(applicant));

        ApplicantProfileResponse response = applicantService.getMyProfile(
                new AuthenticatedUser(1, UserRole.APPLICANT)
        );

        assertThat(response.id()).isEqualTo(1);
        assertThat(response.email()).isEqualTo("applicant@example.com");
        assertThat(response.fullName()).isEqualTo("Nguyen Van A");
        assertThat(response.phone()).isEqualTo("0912345678");
        assertThat(response.location()).isEqualTo("Ha Noi");
    }

    @Test
    void updateMyProfileUpdatesAllowedFields() {
        Applicant applicant = applicant();
        when(applicantRepository.findById(1)).thenReturn(Optional.of(applicant));
        when(applicantRepository.saveAndFlush(applicant)).thenReturn(applicant);

        ApplicantProfileResponse response = applicantService.updateMyProfile(
                new AuthenticatedUser(1, UserRole.APPLICANT),
                new ApplicantProfileUpdateRequest(
                        "Nguyen Van B",
                        "0987654321",
                        "Ho Chi Minh"
                )
        );

        assertThat(response.fullName()).isEqualTo("Nguyen Van B");
        assertThat(response.phone()).isEqualTo("0987654321");
        assertThat(response.location()).isEqualTo("Ho Chi Minh");
    }

    @Test
    void getMyProfileRejectsUnknownApplicant() {
        when(applicantRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicantService.getMyProfile(
                new AuthenticatedUser(99, UserRole.APPLICANT)
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Applicant not found");
    }

    private Applicant applicant() {
        Applicant applicant = new Applicant();
        applicant.setId(1);
        applicant.setEmail("applicant@example.com");
        applicant.setFullName("Nguyen Van A");
        applicant.setPhone("0912345678");
        applicant.setLocation("Ha Noi");
        return applicant;
    }
}
