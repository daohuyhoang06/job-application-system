package com.example.jobapp.service;

import com.example.jobapp.dto.request.CreateJobRequest;
import com.example.jobapp.dto.request.UpdateJobRequest;
import com.example.jobapp.dto.response.EmployerJobResponse;
import com.example.jobapp.dto.response.PageResponse;
import com.example.jobapp.entity.Employer;
import com.example.jobapp.entity.Job;
import com.example.jobapp.entity.enums.JobStatus;
import com.example.jobapp.entity.enums.PositionLevel;
import com.example.jobapp.exception.InvalidJobDataException;
import com.example.jobapp.exception.ResourceNotFoundException;
import com.example.jobapp.repository.EmployerRepository;
import com.example.jobapp.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployerJobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private EmployerRepository employerRepository;

    @InjectMocks
    private EmployerJobService employerJobService;

    @Test
    void createJobUsesAuthenticatedEmployerAndDefaultsToOpen() {
        Employer employer = employer(7);
        CreateJobRequest request = new CreateJobRequest(
                " Software ",
                " Java Developer ",
                " Build backend services ",
                " Ha Noi ",
                PositionLevel.JUNIOR,
                15_000_000L,
                25_000_000L
        );
        when(employerRepository.findById(7)).thenReturn(Optional.of(employer));
        when(jobRepository.saveAndFlush(any(Job.class))).thenAnswer(invocation -> {
            Job job = invocation.getArgument(0);
            job.setId(11);
            return job;
        });

        EmployerJobResponse response = employerJobService.createJob(7, request);

        ArgumentCaptor<Job> captor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository).saveAndFlush(captor.capture());
        Job saved = captor.getValue();
        assertThat(saved.getEmployer()).isSameAs(employer);
        assertThat(saved.getTitle()).isEqualTo("Java Developer");
        assertThat(saved.getStatus()).isEqualTo(JobStatus.OPEN);
        assertThat(response.id()).isEqualTo(11);
        assertThat(response.employerId()).isEqualTo(7);
    }

    @Test
    void createJobRejectsInvalidSalaryRangeBeforeWriting() {
        CreateJobRequest request = new CreateJobRequest(
                "Software",
                "Java Developer",
                "Build backend services",
                "Ha Noi",
                PositionLevel.JUNIOR,
                30_000_000L,
                20_000_000L
        );

        assertThatThrownBy(() -> employerJobService.createJob(7, request))
                .isInstanceOf(InvalidJobDataException.class)
                .hasMessageContaining("Maximum salary");

        verify(employerRepository, never()).findById(any());
        verify(jobRepository, never()).saveAndFlush(any());
    }

    @Test
    void getJobsFiltersByEmployerAndUsesDescendingCreationTime() {
        Job job = job(11, employer(7));
        PageRequest expectedPage = PageRequest.of(
                0,
                20,
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC,
                        "createdAt"
                )
        );
        when(jobRepository.findAllByEmployerId(7, expectedPage))
                .thenReturn(new PageImpl<>(List.of(job), expectedPage, 1));

        PageResponse<EmployerJobResponse> response = employerJobService.getJobs(7, 0, 20);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().getFirst().id()).isEqualTo(11);
        assertThat(response.totalElements()).isEqualTo(1);
        verify(jobRepository).findAllByEmployerId(7, expectedPage);
    }

    @Test
    void getJobsRejectsInvalidPagination() {
        assertThatThrownBy(() -> employerJobService.getJobs(7, -1, 20))
                .isInstanceOf(InvalidJobDataException.class)
                .hasMessage("Page must not be negative");
        assertThatThrownBy(() -> employerJobService.getJobs(7, 0, 101))
                .isInstanceOf(InvalidJobDataException.class)
                .hasMessage("Size must be between 1 and 100");

        verify(jobRepository, never()).findAllByEmployerId(any(), any(Pageable.class));
    }

    @Test
    void getJobReturnsOnlyOwnedJob() {
        Job job = job(11, employer(7));
        when(jobRepository.findByIdAndEmployerId(11, 7)).thenReturn(Optional.of(job));

        EmployerJobResponse response = employerJobService.getJob(7, 11);

        assertThat(response.id()).isEqualTo(11);
        assertThat(response.employerId()).isEqualTo(7);
    }

    @Test
    void getJobHidesMissingOrForeignJobAsNotFound() {
        when(jobRepository.findByIdAndEmployerId(11, 8)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employerJobService.getJob(8, 11))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Job not found");
    }

    @Test
    void updateJobOnlyChangesProvidedFields() {
        Job job = job(11, employer(7));
        job.setTitle("Old title");
        job.setLocation("Ha Noi");
        job.setSalaryMin(10_000_000L);
        job.setSalaryMax(20_000_000L);
        UpdateJobRequest request = new UpdateJobRequest(
                null,
                " New title ",
                null,
                null,
                null,
                15_000_000L,
                25_000_000L,
                JobStatus.CLOSED
        );
        when(jobRepository.findByIdAndEmployerId(11, 7)).thenReturn(Optional.of(job));
        when(jobRepository.saveAndFlush(job)).thenReturn(job);

        EmployerJobResponse response = employerJobService.updateJob(7, 11, request);

        assertThat(response.title()).isEqualTo("New title");
        assertThat(response.location()).isEqualTo("Ha Noi");
        assertThat(response.salaryMin()).isEqualTo(15_000_000L);
        assertThat(response.status()).isEqualTo(JobStatus.CLOSED);
    }

    @Test
    void updateJobRejectsEmptyRequest() {
        UpdateJobRequest request = new UpdateJobRequest(
                null, null, null, null, null, null, null, null
        );

        assertThatThrownBy(() -> employerJobService.updateJob(7, 11, request))
                .isInstanceOf(InvalidJobDataException.class)
                .hasMessageContaining("At least one field");

        verify(jobRepository, never()).findByIdAndEmployerId(any(), any());
    }

    @Test
    void closeJobSetsClosedWithoutDeletingJobOrApplications() {
        Job job = job(11, employer(7));
        job.setStatus(JobStatus.OPEN);
        when(jobRepository.findByIdAndEmployerId(11, 7)).thenReturn(Optional.of(job));
        when(jobRepository.saveAndFlush(job)).thenReturn(job);

        employerJobService.closeJob(7, 11);

        assertThat(job.getStatus()).isEqualTo(JobStatus.CLOSED);
        verify(jobRepository).saveAndFlush(job);
        verify(jobRepository, never()).delete(any(Job.class));
        verify(jobRepository, never()).deleteById(any());
    }

    @Test
    void closeJobIsIdempotentWhenAlreadyClosed() {
        Job job = job(11, employer(7));
        job.setStatus(JobStatus.CLOSED);
        when(jobRepository.findByIdAndEmployerId(11, 7)).thenReturn(Optional.of(job));

        employerJobService.closeJob(7, 11);

        verify(jobRepository, never()).saveAndFlush(any());
        verify(jobRepository, never()).delete(any(Job.class));
    }

    private Employer employer(Integer id) {
        Employer employer = new Employer();
        employer.setId(id);
        return employer;
    }

    private Job job(Integer id, Employer employer) {
        Job job = new Job();
        job.setId(id);
        job.setEmployer(employer);
        job.setCategory("Software");
        job.setTitle("Java Developer");
        job.setDescription("Build backend services");
        job.setLocation("Ha Noi");
        job.setPositionLevel(PositionLevel.JUNIOR);
        job.setSalaryMin(15_000_000L);
        job.setSalaryMax(25_000_000L);
        job.setStatus(JobStatus.OPEN);
        return job;
    }
}
