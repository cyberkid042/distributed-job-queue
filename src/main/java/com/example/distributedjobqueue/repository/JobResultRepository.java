package com.example.distributedjobqueue.repository;

import com.example.distributedjobqueue.model.Job;
import com.example.distributedjobqueue.model.JobResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for JobResult entity operations.
 */
@Repository
public interface JobResultRepository extends JpaRepository<JobResult, Long> {

    /**
     * Find all results for a specific job.
     */
    List<JobResult> findByJob(Job job);

    /**
     * Find all results for a job by job ID.
     */
    List<JobResult> findByJobId(Long jobId);

    /**
     * Find the latest result for a job.
     */
    Optional<JobResult> findFirstByJobOrderByCreatedAtDesc(Job job);

    /**
     * Find the latest result for a job by job ID.
     */
    Optional<JobResult> findFirstByJobIdOrderByCreatedAtDesc(Long jobId);

    /**
     * Find results with execution time greater than a threshold.
     */
    @Query("SELECT jr FROM JobResult jr WHERE jr.executionTimeMs > :threshold")
    List<JobResult> findSlowJobResults(@Param("threshold") Long threshold);

    /**
     * Get average execution time for a job type.
     */
    @Query("SELECT AVG(jr.executionTimeMs) FROM JobResult jr JOIN jr.job j WHERE j.jobType = :jobType")
    Optional<Double> getAverageExecutionTimeByJobType(@Param("jobType") String jobType);

    /**
     * Count results for a specific job.
     */
    long countByJob(Job job);

    /**
     * Count results for a job by job ID.
     */
    long countByJobId(Long jobId);
}
