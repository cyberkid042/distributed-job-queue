package com.example.distributedjobqueue.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity representing a job in the distributed job queue system.
 */
@Entity
@Table(name = "jobs", indexes = {
    @Index(name = "idx_jobs_status", columnList = "status"),
    @Index(name = "idx_jobs_job_id", columnList = "job_id"),
    @Index(name = "idx_jobs_created_at", columnList = "created_at"),
    @Index(name = "idx_jobs_priority", columnList = "priority DESC")
})
public class Job {

    private static final Logger logger = LoggerFactory.getLogger(Job.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false, unique = true, length = 255)
    private String jobId;

    @Column(name = "job_type", nullable = false, length = 100)
    private String jobType;

    @Column(name = "payload", nullable = false, columnDefinition = "VARCHAR(4000)")
    private String payloadJson;

    @Transient
    private Map<String, Object> payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private JobStatus status = JobStatus.PENDING;

    @Column(name = "priority", nullable = false)
    private Integer priority = 0;

    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries = 3;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "error_message", columnDefinition = "VARCHAR(4000)")
    private String errorMessage;

    @Column(name = "worker_id", length = 255)
    private String workerId;

    // Constructors
    public Job() {}

    public Job(String jobId, String jobType, Map<String, Object> payload) {
        this.jobId = jobId;
        this.jobType = jobType;
        this.payload = payload != null ? new HashMap<>(payload) : new HashMap<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        serializePayload();
    }

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        serializePayload();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        serializePayload();
    }

    @PostLoad
    protected void onLoad() {
        deserializePayload();
    }

    // JSON serialization methods
    private void serializePayload() {
        if (payload != null) {
            try {
                this.payloadJson = objectMapper.writeValueAsString(payload);
            } catch (JsonProcessingException e) {
                logger.error("Failed to serialize payload for job {}", jobId, e);
                this.payloadJson = "{}";
            }
        }
    }

    private void deserializePayload() {
        if (payloadJson != null && !payloadJson.trim().isEmpty()) {
            try {
                this.payload = objectMapper.readValue(payloadJson, new TypeReference<Map<String, Object>>() {});
            } catch (JsonProcessingException e) {
                logger.error("Failed to deserialize payload for job {}", jobId, e);
                this.payload = new HashMap<>();
            }
        } else {
            this.payload = new HashMap<>();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public Map<String, Object> getPayload() {
        if (payload == null) {
            deserializePayload();
        }
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload != null ? new HashMap<>(payload) : new HashMap<>();
        serializePayload();
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    // Utility methods
    public boolean canRetry() {
        return retryCount < maxRetries;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    @Override
    public String toString() {
        return "Job{" +
                "id=" + id +
                ", jobId='" + jobId + '\'' +
                ", jobType='" + jobType + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", retryCount=" + retryCount +
                ", maxRetries=" + maxRetries +
                ", createdAt=" + createdAt +
                ", workerId='" + workerId + '\'' +
                '}';
    }
}
