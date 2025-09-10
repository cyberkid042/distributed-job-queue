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
 * Entity representing the result of a job execution.
 */
@Entity
@Table(name = "job_results", indexes = {
    @Index(name = "idx_job_results_job_id", columnList = "job_id")
})
public class JobResult {

    private static final Logger logger = LoggerFactory.getLogger(JobResult.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "result", columnDefinition = "VARCHAR(4000)")
    private String resultJson;

    @Transient
    private Map<String, Object> result;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public JobResult() {}

    public JobResult(Job job, Map<String, Object> result, Long executionTimeMs) {
        this.job = job;
        this.result = result != null ? new HashMap<>(result) : null;
        this.executionTimeMs = executionTimeMs;
        this.createdAt = LocalDateTime.now();
        serializeResult();
    }

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        serializeResult();
    }

    @PreUpdate
    protected void onUpdate() {
        serializeResult();
    }

    @PostLoad
    protected void onLoad() {
        deserializeResult();
    }

    // JSON serialization methods
    private void serializeResult() {
        if (result != null) {
            try {
                this.resultJson = objectMapper.writeValueAsString(result);
            } catch (JsonProcessingException e) {
                logger.error("Failed to serialize result for job result {}", id, e);
                this.resultJson = "{}";
            }
        }
    }

    private void deserializeResult() {
        if (resultJson != null && !resultJson.trim().isEmpty()) {
            try {
                this.result = objectMapper.readValue(resultJson, new TypeReference<Map<String, Object>>() {});
            } catch (JsonProcessingException e) {
                logger.error("Failed to deserialize result for job result {}", id, e);
                this.result = new HashMap<>();
            }
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Map<String, Object> getResult() {
        if (result == null && resultJson != null) {
            deserializeResult();
        }
        return result;
    }

    public void setResult(Map<String, Object> result) {
        this.result = result != null ? new HashMap<>(result) : null;
        serializeResult();
    }

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "JobResult{" +
                "id=" + id +
                ", jobId=" + (job != null ? job.getId() : null) +
                ", executionTimeMs=" + executionTimeMs +
                ", createdAt=" + createdAt +
                '}';
    }
}
