package com.example.jobapp.service;

import com.example.jobapp.dto.request.ApplicantRegisterRequest;
import com.example.jobapp.dto.request.EmployerRegisterRequest;
import com.example.jobapp.dto.request.LoginRequest;
import com.example.jobapp.dto.response.ApplicantRegisterResponse;
import com.example.jobapp.dto.response.AuthResponse;
import com.example.jobapp.dto.response.AuthUserResponse;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final ApplicantRepository applicantRepository;
    private final EmployerRepository employerRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            ApplicantRepository applicantRepository,
            EmployerRepository employerRepository,
            CompanyRepository companyRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.applicantRepository = applicantRepository;
        this.employerRepository = employerRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public ApplicantRegisterResponse registerApplicant(ApplicantRegisterRequest request) {
        String email = normalizeRequired(request.email());
        if (applicantRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateEmailException();
        }

        Applicant applicant = new Applicant();
        applicant.setEmail(email);
        applicant.setPasswordHash(passwordEncoder.encode(request.password()));
        applicant.setFullName(normalizeRequired(request.fullName()));
        applicant.setPhone(normalizeOptional(request.phone()));
        applicant.setLocation(normalizeOptional(request.location()));

        Applicant saved = applicantRepository.saveAndFlush(applicant);
        return new ApplicantRegisterResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getFullName(),
                saved.getPhone(),
                saved.getLocation(),
                saved.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse loginApplicant(LoginRequest request) {
        Applicant applicant = applicantRepository.findByEmailIgnoreCase(normalizeRequired(request.email()))
                .orElseThrow(InvalidCredentialsException::new);
        verifyPassword(request.password(), applicant.getPasswordHash());

        return createAuthResponse(
                applicant.getId(),
                applicant.getEmail(),
                applicant.getFullName(),
                UserRole.APPLICANT,
                null
        );
    }

    @Transactional
    public EmployerRegisterResponse registerEmployer(EmployerRegisterRequest request) {
        String email = normalizeRequired(request.email());
        if (employerRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateEmailException();
        }

        Company company = new Company();
        company.setName(normalizeRequired(request.company().name()));
        company.setDescription(request.company().description().trim());
        Company savedCompany = companyRepository.saveAndFlush(company);

        Employer employer = new Employer();
        employer.setCompany(savedCompany);
        employer.setEmail(email);
        employer.setPasswordHash(passwordEncoder.encode(request.password()));
        employer.setFullName(normalizeRequired(request.fullName()));
        Employer savedEmployer = employerRepository.saveAndFlush(employer);

        return new EmployerRegisterResponse(
                savedEmployer.getId(),
                savedEmployer.getEmail(),
                savedEmployer.getFullName(),
                new EmployerRegisterResponse.CompanyResponse(
                        savedCompany.getId(),
                        savedCompany.getName(),
                        savedCompany.getDescription()
                ),
                savedEmployer.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse loginEmployer(LoginRequest request) {
        Employer employer = employerRepository.findByEmailIgnoreCase(normalizeRequired(request.email()))
                .orElseThrow(InvalidCredentialsException::new);
        verifyPassword(request.password(), employer.getPasswordHash());

        return createAuthResponse(
                employer.getId(),
                employer.getEmail(),
                employer.getFullName(),
                UserRole.EMPLOYER,
                employer.getCompany().getId()
        );
    }

    private AuthResponse createAuthResponse(
            Integer id,
            String email,
            String fullName,
            UserRole role,
            Integer companyId
    ) {
        String token = jwtService.generateToken(id, role);
        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationSeconds(),
                new AuthUserResponse(id, email, fullName, role, companyId)
        );
    }

    private void verifyPassword(String rawPassword, String passwordHash) {
        if (!passwordEncoder.matches(rawPassword, passwordHash)) {
            throw new InvalidCredentialsException();
        }
    }

    private String normalizeRequired(String value) {
        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
