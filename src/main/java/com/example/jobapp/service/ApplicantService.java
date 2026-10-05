package com.example.jobapp.service;
import com.example.jobapp.repository.ApplicantRepository;
import com.example.jobapp.dto.response.ApplicantProfileResponse;
import com.example.jobapp.entity.Applicant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.jobapp.security.AuthenticatedUser;
import com.example.jobapp.dto.request.ApplicantProfileUpdateRequest;
@Service
public class ApplicantService {
    private final ApplicantRepository applicantRepository;

    public ApplicantService(ApplicantRepository applicantRepository) {
        this.applicantRepository = applicantRepository;
    }

    @Transactional(readOnly = true)
    public ApplicantProfileResponse getMyProfile(AuthenticatedUser authenticatedUser) {
        Applicant applicant = applicantRepository.findById(authenticatedUser.userId())
                .orElseThrow(() -> new IllegalStateException("Applicant not found"));
        return new ApplicantProfileResponse(
                applicant.getId(),
                applicant.getEmail(),
                applicant.getFullName(),
                applicant.getPhone(),
                applicant.getLocation()
        );
    }

    @Transactional
    public ApplicantProfileResponse updateMyProfile(AuthenticatedUser authenticatedUser, ApplicantProfileUpdateRequest request) {
        Applicant applicant = applicantRepository.findById(authenticatedUser.userId())
                .orElseThrow(() -> new IllegalStateException("Applicant not found"));
        if (request.fullName() != null) {
            applicant.setFullName(request.fullName());
        }
        if (request.phone() != null) {
            applicant.setPhone(request.phone());
        }
        if (request.location() != null) {
            applicant.setLocation(request.location());
        }

        Applicant updatedApplicant = applicantRepository.saveAndFlush(applicant);
        
        return new ApplicantProfileResponse(
                updatedApplicant.getId(),
                updatedApplicant.getEmail(),
                updatedApplicant.getFullName(),
                updatedApplicant.getPhone(),
                updatedApplicant.getLocation()
        );
    }
}
