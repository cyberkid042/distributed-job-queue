package com.example.distributedjobqueue.controller;

import com.example.distributedjobqueue.service.JobMetricsService;
import com.example.distributedjobqueue.service.JobService;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom actuator endpoint for job queue metrics.
 * Provides detailed metrics about job processing and queue status.
 */
@Component
@Endpoint(id = "job-queue")
public class JobQueueMetricsEndpoint {

    private final JobService jobService;
    private final JobMetricsService jobMetricsService;

    public JobQueueMetricsEndpoint(JobService jobService, JobMetricsService jobMetricsService) {
        this.jobService = jobService;
        this.jobMetricsService = jobMetricsService;
    }

    /**
     * Get detailed job queue metrics.
     */
    @ReadOperation
    public Map<String, Object> getJobQueueMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // Get current statistics
        JobService.JobStatistics stats = jobService.getJobStatistics();

        // Queue metrics
        Map<String, Object> queueMetrics = new HashMap<>();
        queueMetrics.put("size", jobMetricsService.getQueueSize());
        queueMetrics.put("pending", stats.getPending());
        queueMetrics.put("processing", stats.getProcessing());
        queueMetrics.put("completed", stats.getCompleted());
        queueMetrics.put("failed", stats.getFailed());
        queueMetrics.put("total", stats.getTotal());

        // Job processing metrics
        Map<String, Object> processingMetrics = new HashMap<>();
        processingMetrics.put("created", jobMetricsService.getJobsCreatedCount());
        processingMetrics.put("completed", jobMetricsService.getJobsCompletedCount());
        processingMetrics.put("failed", jobMetricsService.getJobsFailedCount());
        processingMetrics.put("retried", jobMetricsService.getJobsRetriedCount());

        // Success rate calculation
        double totalProcessed = jobMetricsService.getJobsCompletedCount() + jobMetricsService.getJobsFailedCount();
        double successRate = totalProcessed > 0 ? (jobMetricsService.getJobsCompletedCount() / totalProcessed) * 100 : 0.0;

        Map<String, Object> performanceMetrics = new HashMap<>();
        performanceMetrics.put("successRate", String.format("%.2f%%", successRate));
        performanceMetrics.put("totalProcessed", totalProcessed);

        // Combine all metrics
        metrics.put("queue", queueMetrics);
        metrics.put("processing", processingMetrics);
        metrics.put("performance", performanceMetrics);

        return metrics;
    }
}
