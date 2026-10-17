package com.example.jobapp.service;

import com.example.jobapp.dto.response.ApplicantApplicationResponse;
import com.example.jobapp.dto.response.StoredResumeResponse;
import com.example.jobapp.entity.Applicant;
import com.example.jobapp.entity.Application;
import com.example.jobapp.entity.Job;
import com.example.jobapp.entity.enums.ApplicationStatus;
import com.example.jobapp.entity.enums.JobStatus;
import com.example.jobapp.exception.ResourceNotFoundException;
import com.example.jobapp.repository.ApplicantRepository;
import com.example.jobapp.repository.ApplicationRepository;
import com.example.jobapp.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class ApplicantApplicationService {
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final ApplicantRepository applicantRepository;
    private final MinioResumeStorageService resumeStorageService;

    public ApplicantApplicationService(
            ApplicationRepository applicationRepository,
            JobRepository jobRepository,
            ApplicantRepository applicantRepository,
            MinioResumeStorageService resumeStorageService
    ) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.applicantRepository = applicantRepository;
        this.resumeStorageService = resumeStorageService;
    }

    @Transactional
    public ApplicantApplicationResponse applyJob(
            Integer applicantId,
            Integer jobId,
            MultipartFile file,
            String coverLetter
    ) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        if (job.getStatus() != JobStatus.OPEN) {
            throw new IllegalArgumentException("Cannot apply to a closed job");
        }

        // kiểm tra xem đã ứng tuyển trước đó chưa?
        if (applicationRepository.existsByJob_IdAndApplicant_Id(jobId, applicantId)) {
            throw new IllegalArgumentException("You have already applied for this job");
        }

        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(()->new ResourceNotFoundException("Applicant not found"));

        StoredResumeResponse resume = resumeStorageService.upload(applicantId, file);
        Application application = new Application();
        application.setJob(job);
        application.setApplicant(applicant);
        application.setResumeObjectKey(resume.objectKey());
        application.setResumeOriginalFilename(resume.originalFilename());
        application.setResumeContentType(resume.contentType());
        application.setResumeSizeBytes(resume.sizeBytes());
        application.setResumeUploadedAt(resume.uploadedAt());
        application.setCoverLetter(coverLetter);
        application.setStatus(ApplicationStatus.PENDING);

        try {
            Application saveApplication = applicationRepository.save(application);
            return toResponse(saveApplication);
        } catch (RuntimeException exception) {
            resumeStorageService.deleteQuietly(resume.objectKey());
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<ApplicantApplicationResponse> getMyApplications(Integer applicantId) {
        List<ApplicantApplicationResponse> listApplicantApplication = new ArrayList<>();
        List<Application> listApplication = applicationRepository.findAllByApplicant_IdOrderByAppliedAtDesc(applicantId);
        for (Application it : listApplication) {
            listApplicantApplication.add(toResponse(it));
        }
        return listApplicantApplication;
    }


    @Transactional(readOnly = true)
    public ApplicantApplicationResponse getApplicationById(Integer applicationId, Integer applicantId) {
        Application application = applicationRepository.findByIdAndApplicant_Id(applicationId, applicantId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));
        return toResponse(application);
    }

    @Transactional
    public void withdrawApplication(Integer applicantId, Integer applicationId) {
        Application application = applicationRepository.findByIdAndApplicant_Id(applicationId, applicantId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));
        applicationRepository.delete(application);
    }

    private ApplicantApplicationResponse toResponse(Application application) {
        return new ApplicantApplicationResponse(
                application.getId(),
                application.getJob().getId(),
                application.getJob().getTitle(),
                application.getJob().getEmployer().getCompany().getName(),
                application.getResumeObjectKey(),
                application.getResumeOriginalFilename(),
                application.getResumeContentType(),
                application.getResumeSizeBytes(),
                application.getCoverLetter(),
                application.getStatus(),
                application.getAppliedAt()
        );
    }

}
