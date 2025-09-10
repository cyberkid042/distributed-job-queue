package com.example.distributedjobqueue.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for the job queue application.
 */
@Component
@ConfigurationProperties(prefix = "app.job-queue")
public class JobQueueProperties {

    /**
     * Maximum number of retries for failed jobs.
     */
    private int maxRetries = 3;

    /**
     * Default priority for jobs.
     */
    private int defaultPriority = 0;

    /**
     * Timeout in minutes for worker processing (used to detect stuck jobs).
     */
    private int workerTimeoutMinutes = 30;

    /**
     * Batch size for processing multiple jobs.
     */
    private int batchSize = 10;

    /**
     * Timeout in seconds for job processing.
     */
    private int processingTimeoutSeconds = 300;

    // Getters and Setters
    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getDefaultPriority() {
        return defaultPriority;
    }

    public void setDefaultPriority(int defaultPriority) {
        this.defaultPriority = defaultPriority;
    }

    public int getWorkerTimeoutMinutes() {
        return workerTimeoutMinutes;
    }

    public void setWorkerTimeoutMinutes(int workerTimeoutMinutes) {
        this.workerTimeoutMinutes = workerTimeoutMinutes;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getProcessingTimeoutSeconds() {
        return processingTimeoutSeconds;
    }

    public void setProcessingTimeoutSeconds(int processingTimeoutSeconds) {
        this.processingTimeoutSeconds = processingTimeoutSeconds;
    }
}
