package com.example.distributedjobqueue.controller.dto;

/**
 * DTO for job statistics response.
 */
public class JobStatisticsResponse {

    private long pending;
    private long processing;
    private long completed;
    private long failed;
    private long total;

    public JobStatisticsResponse() {}

    public JobStatisticsResponse(long pending, long processing, long completed, long failed) {
        this.pending = pending;
        this.processing = processing;
        this.completed = completed;
        this.failed = failed;
        this.total = pending + processing + completed + failed;
    }

    // Getters and Setters
    public long getPending() {
        return pending;
    }

    public void setPending(long pending) {
        this.pending = pending;
    }

    public long getProcessing() {
        return processing;
    }

    public void setProcessing(long processing) {
        this.processing = processing;
    }

    public long getCompleted() {
        return completed;
    }

    public void setCompleted(long completed) {
        this.completed = completed;
    }

    public long getFailed() {
        return failed;
    }

    public void setFailed(long failed) {
        this.failed = failed;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
