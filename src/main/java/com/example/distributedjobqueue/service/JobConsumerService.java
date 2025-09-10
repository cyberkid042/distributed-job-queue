package com.example.distributedjobqueue.service;

import com.example.distributedjobqueue.model.Job;
import com.example.distributedjobqueue.model.JobStatus;
import com.example.distributedjobqueue.service.JobMetricsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Kafka consumer service for processing jobs from the distributed queue.
 * Handles job execution and status updates asynchronously.
 */
@Service
public class JobConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(JobConsumerService.class);
    private static final String JOB_TOPIC = "job-queue";

    private final JobService jobService;
    private final ObjectMapper objectMapper;
    private final JobMetricsService jobMetricsService;

    public JobConsumerService(JobService jobService, ObjectMapper objectMapper, JobMetricsService jobMetricsService) {
        this.jobService = jobService;
        this.objectMapper = objectMapper;
        this.jobMetricsService = jobMetricsService;
    }

    /**
     * Process jobs from Kafka topic.
     * This method is called automatically when messages arrive on the job-queue topic.
     */
    @KafkaListener(topics = JOB_TOPIC, groupId = "job-queue-group", containerFactory = "kafkaListenerContainerFactory")
    public void processJob(@Payload String jobJson,
                          @Header(KafkaHeaders.RECEIVED_KEY) String key,
                          @Header(KafkaHeaders.OFFSET) long offset,
                          Acknowledgment acknowledgment) {

        logger.info("Received job message from Kafka. Key: {}, Offset: {}", key, offset);

        try {
            // Parse the job from JSON
            Job job = objectMapper.readValue(jobJson, Job.class);
            logger.info("Processing job: {} of type: {}", job.getJobId(), job.getJobType());

            // Generate a unique worker ID for this processing instance
            String workerId = "worker-" + UUID.randomUUID().toString().substring(0, 8);

            // Start processing the job
            boolean started = jobService.startJobProcessing(job.getId(), workerId);
            if (!started) {
                logger.warn("Failed to start processing job: {}", job.getJobId());
                acknowledgment.acknowledge();
                return;
            }

            // Process the job based on its type
            long startTime = System.currentTimeMillis();
            boolean success = processJobByType(job);
            long processingTime = System.currentTimeMillis() - startTime;

            // Track job processing metrics
            jobMetricsService.recordJobProcessingDuration(java.time.Duration.ofMillis(processingTime));
            jobMetricsService.incrementJobTypeProcessed(job.getJobType());

            // Update job status based on processing result
            if (success) {
                boolean completed = jobService.completeJob(job.getId());
                if (completed) {
                    logger.info("Successfully completed job: {}", job.getJobId());
                } else {
                    logger.error("Failed to mark job as completed: {}", job.getJobId());
                }
            } else {
                // Handle job failure and potential retry
                handleJobFailure(job);
            }

        } catch (JsonProcessingException e) {
            logger.error("Failed to parse job JSON: {}", jobJson, e);
        } catch (Exception e) {
            logger.error("Unexpected error processing job from Kafka", e);
        } finally {
            // Always acknowledge the message to prevent reprocessing
            acknowledgment.acknowledge();
        }
    }

    /**
     * Process a job based on its type.
     * This is where you would implement the actual business logic for different job types.
     */
    private boolean processJobByType(Job job) {
        logger.info("Processing job of type: {} with payload: {}", job.getJobType(), job.getPayload());

        try {
            switch (job.getJobType().toLowerCase()) {
                case "email-job":
                    return processEmailJob(job);
                case "data-processing":
                    return processDataJob(job);
                case "file-processing":
                    return processFileJob(job);
                case "test-job":
                    return processTestJob(job);
                default:
                    logger.warn("Unknown job type: {}", job.getJobType());
                    return false;
            }
        } catch (Exception e) {
            logger.error("Error processing job {} of type {}", job.getJobId(), job.getJobType(), e);
            return false;
        }
    }

    /**
     * Process an email job.
     */
    private boolean processEmailJob(Job job) {
        logger.info("Processing email job: {}", job.getJobId());

        // Simulate email processing
        try {
            Thread.sleep(1000); // Simulate processing time

            // Extract email details from payload
            @SuppressWarnings("unchecked")
            var payload = (java.util.Map<String, Object>) job.getPayload().get("data");
            String email = (String) payload.get("email");
            String subject = (String) payload.get("subject");

            logger.info("Sending email to: {} with subject: {}", email, subject);

            // Here you would integrate with actual email service
            // For demo purposes, we'll just simulate success
            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Process a data processing job.
     */
    private boolean processDataJob(Job job) {
        logger.info("Processing data job: {}", job.getJobId());

        try {
            Thread.sleep(2000); // Simulate longer processing time

            // Extract data from payload
            @SuppressWarnings("unchecked")
            var payload = (java.util.Map<String, Object>) job.getPayload().get("data");
            String task = (String) payload.get("task");

            logger.info("Processing data task: {}", task);

            // Simulate data processing
            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Process a file processing job.
     */
    private boolean processFileJob(Job job) {
        logger.info("Processing file job: {}", job.getJobId());

        try {
            Thread.sleep(3000); // Simulate file processing time

            // Simulate file processing
            logger.info("File processing completed for job: {}", job.getJobId());
            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Process a test job.
     */
    private boolean processTestJob(Job job) {
        logger.info("Processing test job: {}", job.getJobId());

        try {
            Thread.sleep(500); // Quick test processing

            // Extract test data from payload
            @SuppressWarnings("unchecked")
            var payload = (java.util.Map<String, Object>) job.getPayload().get("data");
            String message = (String) payload.get("message");
            Integer number = (Integer) payload.get("number");

            logger.info("Test job processed - Message: {}, Number: {}", message, number);
            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Handle job failure and determine if it should be retried.
     */
    private void handleJobFailure(Job job) {
        logger.warn("Job {} failed, attempting to handle failure", job.getJobId());

        // Try to retry the job if it hasn't exceeded max retries
        boolean retried = jobService.retryJob(job.getId());
        if (retried) {
            logger.info("Job {} scheduled for retry", job.getJobId());
        } else {
            // Mark as permanently failed
            boolean failed = jobService.failJob(job.getId(), "Job processing failed after all retries");
            if (failed) {
                logger.error("Job {} permanently failed", job.getJobId());
            }
        }
    }
}
