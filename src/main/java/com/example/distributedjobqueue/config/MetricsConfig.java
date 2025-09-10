package com.example.distributedjobqueue.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metrics configuration for monitoring job queue performance.
 * Provides custom metrics for job processing, queue size, and error rates.
 */
@Configuration
public class MetricsConfig {

    /**
     * Counter for total jobs submitted.
     */
    @Bean
    public Counter jobsSubmittedCounter(MeterRegistry registry) {
        return Counter.builder("job_queue_jobs_submitted_total")
                .description("Total number of jobs submitted to the queue")
                .register(registry);
    }

    /**
     * Counter for jobs completed successfully.
     */
    @Bean
    public Counter jobsCompletedCounter(MeterRegistry registry) {
        return Counter.builder("job_queue_jobs_completed_total")
                .description("Total number of jobs completed successfully")
                .register(registry);
    }

    /**
     * Counter for jobs that failed.
     */
    @Bean
    public Counter jobsFailedCounter(MeterRegistry registry) {
        return Counter.builder("job_queue_jobs_failed_total")
                .description("Total number of jobs that failed")
                .register(registry);
    }

    /**
     * Counter for jobs that were retried.
     */
    @Bean
    public Counter jobsRetriedCounter(MeterRegistry registry) {
        return Counter.builder("job_queue_jobs_retried_total")
                .description("Total number of jobs that were retried")
                .register(registry);
    }

    /**
     * Timer for job processing duration.
     */
    @Bean
    public Timer jobProcessingTimer(MeterRegistry registry) {
        return Timer.builder("job_queue_job_processing_duration")
                .description("Time taken to process jobs")
                .register(registry);
    }

    /**
     * Timer for job queue wait time.
     */
    @Bean
    public Timer jobQueueWaitTimer(MeterRegistry registry) {
        return Timer.builder("job_queue_job_wait_duration")
                .description("Time jobs spend waiting in the queue")
                .register(registry);
    }

    /**
     * Gauge for current queue size (pending jobs).
     * Note: This would need to be updated dynamically by a service.
     */
    public void registerQueueSizeGauge(MeterRegistry registry, Runnable gaugeUpdater) {
        Gauge.builder("job_queue_size", gaugeUpdater, (updater) -> {
            // This would be implemented by a service that tracks queue size
            return 0.0;
        })
        .description("Current number of jobs in the queue")
        .register(registry);
    }

    /**
     * Gauge for active workers.
     */
    public void registerActiveWorkersGauge(MeterRegistry registry, Runnable gaugeUpdater) {
        Gauge.builder("job_queue_active_workers", gaugeUpdater, (updater) -> {
            // This would be implemented by a worker manager
            return 0.0;
        })
        .description("Number of currently active workers")
        .register(registry);
    }
}
