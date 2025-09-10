package com.example.distributedjobqueue.repository;

import com.example.distributedjobqueue.model.Job;
import com.example.distributedjobqueue.model.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Job entity operations.
 */
@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    /**
     * Find a job by its unique job ID.
     */
    Optional<Job> findByJobId(String jobId);

    /**
     * Find jobs by status with pagination.
     */
    Page<Job> findByStatus(JobStatus status, Pageable pageable);

    /**
     * Find jobs by status.
     */
    List<Job> findByStatus(JobStatus status);

    /**
     * Find jobs by status and priority (for processing order).
     */
    List<Job> findByStatusOrderByPriorityDescCreatedAtAsc(JobStatus status);

    /**
     * Find jobs that are eligible for retry (failed jobs that haven't exceeded max retries).
     */
    @Query("SELECT j FROM Job j WHERE j.status = :status AND j.retryCount < j.maxRetries")
    List<Job> findRetryableJobs(@Param("status") JobStatus status);

    /**
     * Find jobs that have been in processing state for too long (potential stuck jobs).
     */
    @Query("SELECT j FROM Job j WHERE j.status = 'PROCESSING' AND j.startedAt < :cutoffTime")
    List<Job> findStuckJobs(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Count jobs by status.
     */
    long countByStatus(JobStatus status);

    /**
     * Find the next job to process (pending jobs ordered by priority and creation time).
     * Uses pessimistic write lock to prevent concurrent processing.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT j FROM Job j WHERE j.status = 'PENDING' ORDER BY j.priority DESC, j.createdAt ASC")
    List<Job> findNextPendingJobWithLock(Pageable pageable);

    /**
     * Update job status and timestamps.
     */
    @Modifying
    @Query("UPDATE Job j SET j.status = :status, j.updatedAt = CURRENT_TIMESTAMP WHERE j.id = :id")
    int updateJobStatus(@Param("id") Long id, @Param("status") JobStatus status);

    /**
     * Update job status with started timestamp.
     */
    @Modifying
    @Query("UPDATE Job j SET j.status = :status, j.startedAt = CURRENT_TIMESTAMP, j.updatedAt = CURRENT_TIMESTAMP, j.workerId = :workerId WHERE j.id = :id AND j.status = 'PENDING'")
    int updateJobStatusWithStart(@Param("id") Long id, @Param("status") JobStatus status, @Param("workerId") String workerId);

    /**
     * Update job status with completion timestamp.
     */
    @Modifying
    @Query("UPDATE Job j SET j.status = :status, j.completedAt = CURRENT_TIMESTAMP, j.updatedAt = CURRENT_TIMESTAMP WHERE j.id = :id AND j.status = 'PROCESSING'")
    int updateJobStatusWithCompletion(@Param("id") Long id, @Param("status") JobStatus status);

    /**
     * Update job status with error information.
     */
    @Modifying
    @Query("UPDATE Job j SET j.status = :status, j.errorMessage = :errorMessage, j.updatedAt = CURRENT_TIMESTAMP WHERE j.id = :id AND j.status != 'FAILED'")
    int updateJobStatusWithError(@Param("id") Long id, @Param("status") JobStatus status, @Param("errorMessage") String errorMessage);

    /**
     * Increment retry count for a job.
     */
    @Modifying
    @Query("UPDATE Job j SET j.retryCount = j.retryCount + 1, j.status = 'PENDING', j.updatedAt = CURRENT_TIMESTAMP WHERE j.id = :id")
    int incrementRetryCount(@Param("id") Long id);

    /**
     * Find jobs created within a time range.
     */
    List<Job> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Find jobs by type and status.
     */
    List<Job> findByJobTypeAndStatus(String jobType, JobStatus status);

    /**
     * Find jobs by job type with pagination.
     */
    Page<Job> findByJobType(String jobType, Pageable pageable);

    /**
     * Find jobs by job type and status with pagination.
     */
    Page<Job> findByJobTypeAndStatus(String jobType, JobStatus status, Pageable pageable);
}
