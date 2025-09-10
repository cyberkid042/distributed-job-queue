package com.example.distributedjobqueue.controller;

import com.example.distributedjobqueue.controller.dto.CreateJobRequest;
import com.example.distributedjobqueue.controller.dto.JobResponse;
import com.example.distributedjobqueue.controller.dto.JobStatisticsResponse;
import com.example.distributedjobqueue.model.Job;
import com.example.distributedjobqueue.model.JobStatus;
import com.example.distributedjobqueue.service.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST Controller for job queue operations.
 * Provides endpoints for creating, querying, and managing jobs.
 */
@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private static final Logger logger = LoggerFactory.getLogger(JobController.class);

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    /**
     * Create a new job.
     */
    @PostMapping
    public ResponseEntity<JobResponse> createJob(@RequestBody CreateJobRequest request) {
        try {
            logger.info("Creating new job of type: {}", request.getJobType());

            Job job = jobService.createJob(request.getJobType(), request.getPayload());

            if (request.getPriority() != null) {
                job.setPriority(request.getPriority());
            }

            JobResponse response = mapToJobResponse(job);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Failed to create job", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get a job by its ID.
     */
    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponse> getJob(@PathVariable String jobId) {

        logger.debug("Retrieving job with ID: {}", jobId);

        Optional<Job> jobOpt = jobService.findJobById(jobId);
        if (jobOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        JobResponse response = mapToJobResponse(jobOpt.get());
        return ResponseEntity.ok(response);
    }

    /**
     * List jobs with optional filtering and pagination.
     */
    @GetMapping
    public ResponseEntity<Page<JobResponse>> listJobs(
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) String jobType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        logger.debug("Listing jobs - status: {}, jobType: {}, page: {}, size: {}",
                    status, jobType, page, size);

        Sort.Direction direction = Sort.Direction.fromString(sortDir.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Job> jobsPage = getJobsPage(status, jobType, pageable);

        Page<JobResponse> responsePage = jobsPage.map(this::mapToJobResponse);
        return ResponseEntity.ok(responsePage);
    }

    /**
     * Cancel a job (only if it's in PENDING status).
     */
    @DeleteMapping("/{jobId}")
    public ResponseEntity<Void> cancelJob(@PathVariable String jobId) {

        logger.info("Attempting to cancel job: {}", jobId);

        Optional<Job> jobOpt = jobService.findJobById(jobId);
        if (jobOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Job job = jobOpt.get();
        if (job.getStatus() != JobStatus.PENDING) {
            logger.warn("Cannot cancel job {} with status {}", jobId, job.getStatus());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Mark as failed with cancellation message
        boolean cancelled = jobService.failJob(job.getId(), "Job cancelled by user");
        if (cancelled) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get job statistics.
     */
    @GetMapping("/stats")
    public ResponseEntity<JobStatisticsResponse> getJobStatistics() {
        logger.debug("Retrieving job statistics");

        JobService.JobStatistics stats = jobService.getJobStatistics();
        JobStatisticsResponse response = new JobStatisticsResponse(
            stats.getPending(),
            stats.getProcessing(),
            stats.getCompleted(),
            stats.getFailed()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Helper method to get jobs page with filtering.
     */
    private Page<Job> getJobsPage(JobStatus status, String jobType, Pageable pageable) {
        if (status != null && jobType != null) {
            // Filter by both status and job type
            return jobService.findByJobTypeAndStatus(jobType, status, pageable);
        } else if (status != null) {
            // Filter by status only
            return jobService.findByStatus(status, pageable);
        } else if (jobType != null) {
            // Filter by job type only
            return jobService.findByJobType(jobType, pageable);
        } else {
            // No filters - return all jobs
            return jobService.findAllJobs(pageable);
        }
    }

    /**
     * Map Job entity to JobResponse DTO.
     */
    private JobResponse mapToJobResponse(Job job) {
        JobResponse response = new JobResponse();
        response.setId(job.getId());
        response.setJobId(job.getJobId());
        response.setJobType(job.getJobType());
        response.setPayload(job.getPayload());
        response.setStatus(job.getStatus());
        response.setPriority(job.getPriority());
        response.setMaxRetries(job.getMaxRetries());
        response.setRetryCount(job.getRetryCount());
        response.setCreatedAt(job.getCreatedAt());
        response.setUpdatedAt(job.getUpdatedAt());
        response.setStartedAt(job.getStartedAt());
        response.setCompletedAt(job.getCompletedAt());
        response.setErrorMessage(job.getErrorMessage());
        response.setWorkerId(job.getWorkerId());
        return response;
    }
}
