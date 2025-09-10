package com.example.distributedjobqueue.service;

import com.example.distributedjobqueue.config.JobQueueProperties;
import com.example.distributedjobqueue.model.Job;
import com.example.distributedjobqueue.model.JobStatus;
import com.example.distributedjobqueue.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for managing job operations.
 * Provides methods for creating, updating, and querying jobs.
 */
@Service
@Transactional
public class JobService {

    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    private final JobRepository jobRepository;
    private final JobQueueProperties properties;

    public JobService(JobRepository jobRepository, JobQueueProperties properties) {
        this.jobRepository = jobRepository;
        this.properties = properties;
    }

    /**
     * Create a new job with the given type and payload.
     */
    public Job createJob(String jobType, Object payload) {
        String jobId = UUID.randomUUID().toString();

        Job job = new Job();
        job.setJobId(jobId);
        job.setJobType(jobType);
        job.setPayload(Map.of("data", payload));
        job.setPriority(properties.getDefaultPriority());
        job.setMaxRetries(properties.getMaxRetries());

        Job savedJob = jobRepository.save(job);
        logger.info("Created new job: {}", savedJob);

        return savedJob;
    }

    /**
     * Find a job by its unique job ID.
     */
    @Transactional(readOnly = true)
    public Optional<Job> findJobById(String jobId) {
        return jobRepository.findByJobId(jobId);
    }

    /**
     * Get the next pending job to process.
     */
    public Optional<Job> getNextPendingJob() {
        List<Job> pendingJobs = jobRepository.findNextPendingJobWithLock(
            PageRequest.of(0, 1)
        );

        if (pendingJobs.isEmpty()) {
            return Optional.empty();
        }

        Job job = pendingJobs.get(0);
        logger.debug("Retrieved next pending job: {}", job.getJobId());

        return Optional.of(job);
    }

    /**
     * Mark a job as processing and assign it to a worker.
     */
    public boolean startJobProcessing(Long jobId, String workerId) {
        int updated = jobRepository.updateJobStatusWithStart(jobId, JobStatus.PROCESSING, workerId);
        if (updated > 0) {
            logger.info("Started processing job ID {} with worker {}", jobId, workerId);
            return true;
        }
        return false;
    }

    /**
     * Mark a job as completed.
     */
    public boolean completeJob(Long jobId) {
        int updated = jobRepository.updateJobStatusWithCompletion(jobId, JobStatus.COMPLETED);
        if (updated > 0) {
            logger.info("Completed job ID {}", jobId);
            return true;
        }
        return false;
    }

    /**
     * Mark a job as failed with an error message.
     */
    public boolean failJob(Long jobId, String errorMessage) {
        int updated = jobRepository.updateJobStatusWithError(jobId, JobStatus.FAILED, errorMessage);
        if (updated > 0) {
            logger.warn("Failed job ID {}: {}", jobId, errorMessage);
            return true;
        }
        return false;
    }

    /**
     * Retry a failed job if it hasn't exceeded max retries.
     */
    public boolean retryJob(Long jobId) {
        Optional<Job> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            return false;
        }

        Job job = jobOpt.get();
        if (!job.canRetry()) {
            logger.warn("Job {} has exceeded max retries ({})", jobId, job.getMaxRetries());
            return false;
        }

        int updated = jobRepository.incrementRetryCount(jobId);
        if (updated > 0) {
            logger.info("Retrying job ID {} (attempt {}/{})", jobId,
                       job.getRetryCount() + 1, job.getMaxRetries());
            return true;
        }
        return false;
    }

    /**
     * Find jobs that have been stuck in processing state for too long.
     */
    @Transactional(readOnly = true)
    public List<Job> findStuckJobs() {
        LocalDateTime cutoffTime = LocalDateTime.now()
            .minusMinutes(properties.getWorkerTimeoutMinutes());

        List<Job> stuckJobs = jobRepository.findStuckJobs(cutoffTime);
        logger.info("Found {} stuck jobs", stuckJobs.size());

        return stuckJobs;
    }

    /**
     * Get job statistics.
     */
    @Transactional(readOnly = true)
    public JobStatistics getJobStatistics() {
        long pending = jobRepository.countByStatus(JobStatus.PENDING);
        long processing = jobRepository.countByStatus(JobStatus.PROCESSING);
        long completed = jobRepository.countByStatus(JobStatus.COMPLETED);
        long failed = jobRepository.countByStatus(JobStatus.FAILED);

        return new JobStatistics(pending, processing, completed, failed);
    }

    /**
     * Data class for job statistics.
     */
    public static class JobStatistics {
        private final long pending;
        private final long processing;
        private final long completed;
        private final long failed;

        public JobStatistics(long pending, long processing, long completed, long failed) {
            this.pending = pending;
            this.processing = processing;
            this.completed = completed;
            this.failed = failed;
        }

        public long getPending() { return pending; }
        public long getProcessing() { return processing; }
        public long getCompleted() { return completed; }
        public long getFailed() { return failed; }
        public long getTotal() { return pending + processing + completed + failed; }
    }
}
