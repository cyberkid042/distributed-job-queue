package com.example.distributedjobqueue.service;

import com.example.distributedjobqueue.model.Job;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer service for sending job messages to Kafka.
 * Handles job submission to the distributed processing queue.
 */
@Service
public class JobProducerService {

    private static final Logger logger = LoggerFactory.getLogger(JobProducerService.class);
    private static final String JOB_TOPIC = "job-queue";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public JobProducerService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Send a job to Kafka for asynchronous processing.
     */
    public CompletableFuture<SendResult<String, String>> sendJob(Job job) {
        try {
            String jobJson = objectMapper.writeValueAsString(job);
            logger.info("Sending job {} to Kafka topic: {}", job.getJobId(), JOB_TOPIC);

            return kafkaTemplate.send(JOB_TOPIC, job.getJobId(), jobJson)
                    .whenComplete((result, exception) -> {
                        if (exception == null) {
                            logger.info("Successfully sent job {} to Kafka. Offset: {}",
                                      job.getJobId(), result.getRecordMetadata().offset());
                        } else {
                            logger.error("Failed to send job {} to Kafka", job.getJobId(), exception);
                        }
                    });

        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize job {} to JSON", job.getJobId(), e);
            CompletableFuture<SendResult<String, String>> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }

    /**
     * Send a job with a specific key for partitioning.
     */
    public CompletableFuture<SendResult<String, String>> sendJob(Job job, String key) {
        try {
            String jobJson = objectMapper.writeValueAsString(job);
            logger.info("Sending job {} to Kafka topic: {} with key: {}", job.getJobId(), JOB_TOPIC, key);

            return kafkaTemplate.send(JOB_TOPIC, key, jobJson)
                    .whenComplete((result, exception) -> {
                        if (exception == null) {
                            logger.info("Successfully sent job {} to Kafka with key {}. Offset: {}",
                                      job.getJobId(), key, result.getRecordMetadata().offset());
                        } else {
                            logger.error("Failed to send job {} to Kafka with key {}", job.getJobId(), key, exception);
                        }
                    });

        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize job {} to JSON", job.getJobId(), e);
            CompletableFuture<SendResult<String, String>> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }
}
