package com.example.distributedjobqueue.controller.dto;

import com.example.distributedjobqueue.model.JobStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for job creation requests.
 */
public class CreateJobRequest {

    private String jobType;
    private Map<String, Object> payload;
    private Integer priority;

    public CreateJobRequest() {}

    public CreateJobRequest(String jobType, Map<String, Object> payload) {
        this.jobType = jobType;
        this.payload = payload;
    }

    public CreateJobRequest(String jobType, Map<String, Object> payload, Integer priority) {
        this.jobType = jobType;
        this.payload = payload;
        this.priority = priority;
    }

    // Getters and Setters
    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
