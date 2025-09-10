package com.example.distributedjobqueue.model;

/**
 * Enum representing the possible statuses of a job in the distributed job queue.
 */
public enum JobStatus {
    PENDING("Job is waiting to be processed"),
    PROCESSING("Job is currently being processed"),
    COMPLETED("Job has been completed successfully"),
    FAILED("Job has failed"),
    RETRYING("Job is being retried after failure");

    private final String description;

    JobStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
