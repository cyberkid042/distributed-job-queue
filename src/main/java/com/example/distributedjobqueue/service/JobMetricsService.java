package com.example.distributedjobqueue.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Metrics service for tracking job queue performance and statistics.
 * Provides custom metrics for Prometheus monitoring.
 */
@Service
public class JobMetricsService {

    private static final Logger logger = LoggerFactory.getLogger(JobMetricsService.class);

    // Queue size gauge
    private final AtomicLong queueSize = new AtomicLong(0);

    // Job processing timers
    private final Timer jobProcessingTimer;
    private final Timer.Sample currentJobTimer;

    // Job counters
    private final Counter jobsCreated;
    private final Counter jobsCompleted;
    private final Counter jobsFailed;
    private final Counter jobsRetried;

    // Job type counters
    private final Counter emailJobsProcessed;
    private final Counter dataJobsProcessed;
    private final Counter fileJobsProcessed;
    private final Counter testJobsProcessed;

    public JobMetricsService(MeterRegistry meterRegistry) {
        logger.info("Initializing job metrics service");

        // Register queue size gauge
        Gauge.builder("job_queue_size", queueSize, AtomicLong::get)
                .description("Current number of jobs in the queue")
                .register(meterRegistry);

        // Register job processing timer
        jobProcessingTimer = Timer.builder("job_processing_duration")
                .description("Time taken to process jobs")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        // Initialize current job timer (will be started when job processing begins)
        currentJobTimer = null;

        // Register job counters
        jobsCreated = Counter.builder("jobs_created_total")
                .description("Total number of jobs created")
                .register(meterRegistry);

        jobsCompleted = Counter.builder("jobs_completed_total")
                .description("Total number of jobs completed successfully")
                .register(meterRegistry);

        jobsFailed = Counter.builder("jobs_failed_total")
                .description("Total number of jobs that failed")
                .register(meterRegistry);

        jobsRetried = Counter.builder("jobs_retried_total")
                .description("Total number of jobs that were retried")
                .register(meterRegistry);

        // Register job type counters
        emailJobsProcessed = Counter.builder("jobs_processed_by_type_total")
                .description("Total number of jobs processed by type")
                .tag("job_type", "email")
                .register(meterRegistry);

        dataJobsProcessed = Counter.builder("jobs_processed_by_type_total")
                .description("Total number of jobs processed by type")
                .tag("job_type", "data")
                .register(meterRegistry);

        fileJobsProcessed = Counter.builder("jobs_processed_by_type_total")
                .description("Total number of jobs processed by type")
                .tag("job_type", "file")
                .register(meterRegistry);

        testJobsProcessed = Counter.builder("jobs_processed_by_type_total")
                .description("Total number of jobs processed by type")
                .tag("job_type", "test")
                .register(meterRegistry);

        logger.info("Job metrics service initialized successfully");
    }

    /**
     * Update the current queue size.
     */
    public void updateQueueSize(long size) {
        queueSize.set(size);
        logger.debug("Updated queue size to: {}", size);
    }

    /**
     * Increment the jobs created counter.
     */
    public void incrementJobsCreated() {
        jobsCreated.increment();
        logger.debug("Incremented jobs created counter");
    }

    /**
     * Increment the jobs completed counter.
     */
    public void incrementJobsCompleted() {
        jobsCompleted.increment();
        logger.debug("Incremented jobs completed counter");
    }

    /**
     * Increment the jobs failed counter.
     */
    public void incrementJobsFailed() {
        jobsFailed.increment();
        logger.debug("Incremented jobs failed counter");
    }

    /**
     * Increment the jobs retried counter.
     */
    public void incrementJobsRetried() {
        jobsRetried.increment();
        logger.debug("Incremented jobs retried counter");
    }

    /**
     * Increment the counter for a specific job type.
     */
    public void incrementJobTypeProcessed(String jobType) {
        switch (jobType.toLowerCase()) {
            case "email-job":
                emailJobsProcessed.increment();
                break;
            case "data-processing":
                dataJobsProcessed.increment();
                break;
            case "file-processing":
                fileJobsProcessed.increment();
                break;
            case "test-job":
                testJobsProcessed.increment();
                break;
            default:
                logger.warn("Unknown job type for metrics: {}", jobType);
        }
        logger.debug("Incremented counter for job type: {}", jobType);
    }

    /**
     * Record job processing duration.
     */
    public void recordJobProcessingDuration(Duration duration) {
        jobProcessingTimer.record(duration);
        logger.debug("Recorded job processing duration: {}ms", duration.toMillis());
    }

    /**
     * Record job processing duration from start time.
     */
    public void recordJobProcessingDuration(LocalDateTime startTime) {
        Duration duration = Duration.between(startTime, LocalDateTime.now());
        recordJobProcessingDuration(duration);
    }

    /**
     * Get current queue size.
     */
    public long getQueueSize() {
        return queueSize.get();
    }

    /**
     * Get jobs created count.
     */
    public double getJobsCreatedCount() {
        return jobsCreated.count();
    }

    /**
     * Get jobs completed count.
     */
    public double getJobsCompletedCount() {
        return jobsCompleted.count();
    }

    /**
     * Get jobs failed count.
     */
    public double getJobsFailedCount() {
        return jobsFailed.count();
    }

    /**
     * Get jobs retried count.
     */
    public double getJobsRetriedCount() {
        return jobsRetried.count();
    }
}
