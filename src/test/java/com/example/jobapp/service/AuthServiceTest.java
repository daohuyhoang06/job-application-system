package com.example.jobapp.service;

import com.example.jobapp.dto.request.ApplicantRegisterRequest;
import com.example.jobapp.dto.request.EmployerRegisterRequest;
import com.example.jobapp.dto.request.CompanyRegisterRequest;
import com.example.jobapp.dto.request.LoginRequest;
import com.example.jobapp.dto.response.ApplicantRegisterResponse;
import com.example.jobapp.dto.response.AuthResponse;
import com.example.jobapp.dto.response.EmployerRegisterResponse;
import com.example.jobapp.entity.Applicant;
import com.example.jobapp.entity.Company;
import com.example.jobapp.entity.Employer;
import com.example.jobapp.entity.enums.UserRole;
import com.example.jobapp.exception.DuplicateEmailException;
import com.example.jobapp.exception.InvalidCredentialsException;
import com.example.jobapp.repository.ApplicantRepository;
import com.example.jobapp.repository.CompanyRepository;
import com.example.jobapp.repository.EmployerRepository;
import com.example.jobapp.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private ApplicantRepository applicantRepository;

    @Mock
    private EmployerRepository employerRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerApplicantHashesPasswordAndDoesNotExposeIt() {
        ApplicantRegisterRequest request = new ApplicantRegisterRequest(
                "  APPLICANT@EXAMPLE.COM ",
                "Password123",
                " Nguyen Van A ",
                " 0912345678 ",
                " Ha Noi "
        );
        when(applicantRepository.existsByEmailIgnoreCase("APPLICANT@EXAMPLE.COM"))
                .thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("$2a$hashed-password");
        when(applicantRepository.saveAndFlush(any(Applicant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ApplicantRegisterResponse response = authService.registerApplicant(request);

        ArgumentCaptor<Applicant> captor = ArgumentCaptor.forClass(Applicant.class);
        verify(applicantRepository).saveAndFlush(captor.capture());
        Applicant saved = captor.getValue();

        assertThat(saved.getEmail()).isEqualTo("APPLICANT@EXAMPLE.COM");
        assertThat(saved.getPasswordHash()).isEqualTo("$2a$hashed-password");
        assertThat(saved.getFullName()).isEqualTo("Nguyen Van A");
        assertThat(saved.getPhone()).isEqualTo("0912345678");
        assertThat(saved.getLocation()).isEqualTo("Ha Noi");
        assertThat(response).isNotNull();
    }

    @Test
    void registerApplicantRejectsDuplicateEmail() {
        when(applicantRepository.existsByEmailIgnoreCase("applicant@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerApplicant(new ApplicantRegisterRequest(
                "applicant@example.com", "Password123", "Nguyen Van A", null, null
        )))
                .isInstanceOf(DuplicateEmailException.class);

        verify(applicantRepository, never()).saveAndFlush(any(Applicant.class));
    }

    @Test
    void registerEmployerCreatesCompanyAndEmployer() {
        EmployerRegisterRequest request = new EmployerRegisterRequest(
                "employer@example.com",
                "Password123",
                "Tran Van B",
                new CompanyRegisterRequest("Example Company", "Software company")
        );
        Company company = new Company();
        company.setId(10);
        company.setName("Example Company");
        company.setDescription("Software company");

        when(employerRepository.existsByEmailIgnoreCase("employer@example.com")).thenReturn(false);
        when(companyRepository.saveAndFlush(any(Company.class))).thenReturn(company);
        when(passwordEncoder.encode("Password123")).thenReturn("$2a$hashed-password");
        when(employerRepository.saveAndFlush(any(Employer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EmployerRegisterResponse response = authService.registerEmployer(request);

        ArgumentCaptor<Employer> captor = ArgumentCaptor.forClass(Employer.class);
        verify(employerRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getCompany()).isSameAs(company);
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("$2a$hashed-password");
        assertThat(response.company().id()).isEqualTo(10);
        assertThat(response.company().name()).isEqualTo("Example Company");
    }

    @Test
    void loginApplicantReturnsApplicantRoleAndToken() {
        Applicant applicant = new Applicant();
        applicant.setId(1);
        applicant.setEmail("applicant@example.com");
        applicant.setFullName("Nguyen Van A");
        applicant.setPasswordHash("$2a$hashed-password");

        when(applicantRepository.findByEmailIgnoreCase("applicant@example.com"))
                .thenReturn(java.util.Optional.of(applicant));
        when(passwordEncoder.matches("Password123", "$2a$hashed-password")).thenReturn(true);
        when(jwtService.generateToken(1, UserRole.APPLICANT)).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(86400L);

        AuthResponse response = authService.loginApplicant(
                new LoginRequest("applicant@example.com", "Password123")
        );

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.user().role()).isEqualTo(UserRole.APPLICANT);
        assertThat(response.user().companyId()).isNull();
    }

    @Test
    void loginRejectsWrongPasswordWithGenericCredentialsError() {
        Applicant applicant = new Applicant();
        applicant.setPasswordHash("$2a$hashed-password");
        when(applicantRepository.findByEmailIgnoreCase("applicant@example.com"))
                .thenReturn(java.util.Optional.of(applicant));
        when(passwordEncoder.matches("WrongPassword1", "$2a$hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.loginApplicant(
                new LoginRequest("applicant@example.com", "WrongPassword1")
        ))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }
}
