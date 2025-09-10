package com.example.distributedjobqueue;

import com.example.distributedjobqueue.model.JobStatus;
import com.example.distributedjobqueue.service.JobMetricsService;
import com.example.distributedjobqueue.service.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
class MetricsIntegrationTest {

    @Autowired
    private JobService jobService;

    @Autowired
    private JobMetricsService jobMetricsService;

    @Test
    void testMetricsTracking() throws InterruptedException {
        // Get initial metrics
        double initialCreated = jobMetricsService.getJobsCreatedCount();
        double initialCompleted = jobMetricsService.getJobsCompletedCount();
        double initialFailed = jobMetricsService.getJobsFailedCount();

        // Create a test job
        var job = jobService.createJob("test-job", Map.of("message", "test", "number", 123));

        // Verify job creation metric was incremented
        assertThat(jobMetricsService.getJobsCreatedCount()).isEqualTo(initialCreated + 1);

        // Wait a bit for asynchronous Kafka failure to be processed
        Thread.sleep(100);

        // Refresh job status from database
        var refreshedJob = jobService.findJobById(job.getJobId()).orElse(job);

        // Check if job creation failed due to Kafka unavailability
        if (refreshedJob.getStatus() == JobStatus.FAILED) {
            // Job failed during creation, verify failure metric was incremented
            assertThat(jobMetricsService.getJobsFailedCount()).isEqualTo(initialFailed + 1);
            // Skip processing and completion tests since job is already failed
            return;
        }

        // Start processing the job
        boolean started = jobService.startJobProcessing(job.getId(), "test-worker");
        assertThat(started).isTrue();

        // Complete the job
        boolean completed = jobService.completeJob(job.getId());
        assertThat(completed).isTrue();

        // Verify completion metric was incremented
        assertThat(jobMetricsService.getJobsCompletedCount()).isEqualTo(initialCompleted + 1);

        // Get statistics (this should update queue size metric)
        var stats = jobService.getJobStatistics();

        // Verify queue size is updated
        assertThat(jobMetricsService.getQueueSize()).isEqualTo(stats.getPending() + stats.getProcessing());
    }

    @Test
    void testJobFailureMetrics() throws InterruptedException {
        // Get initial metrics
        double initialFailed = jobMetricsService.getJobsFailedCount();

        // Create a test job
        var job = jobService.createJob("test-job", Map.of("message", "test", "number", 123));

        // Wait a bit for asynchronous Kafka failure to be processed
        Thread.sleep(100);

        // Refresh job status from database
        var refreshedJob = jobService.findJobById(job.getJobId()).orElse(job);

        // Check if job creation already failed due to Kafka not being available
        if (refreshedJob.getStatus() == JobStatus.FAILED) {
            // Job was already failed during creation, so just verify the failure count increased by 1
            assertThat(jobMetricsService.getJobsFailedCount()).isEqualTo(initialFailed + 1);
        } else {
            // Job was created successfully, now fail it explicitly
            boolean failed = jobService.failJob(job.getId(), "Test failure");
            assertThat(failed).isTrue();

            // Verify failure metric was incremented by 1 (total should be initial + 1)
            assertThat(jobMetricsService.getJobsFailedCount()).isEqualTo(initialFailed + 1);
        }
    }
}
