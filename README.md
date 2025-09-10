# Distributed Job Queue

A distributed job queue built in Java using Spring Boot, showcasing concurrency, worker pools, and reliable task execution. Implements retries, scheduling, and monitoring to demonstrate scalable system design principles.

## Architecture

```
REST API → Job Producer → Kafka → Job Consumer → Database
     ↓                                        ↓
Status Check ←─────────────────────────────── Job Status Updates
```

The system now implements a fully distributed architecture:

1. **REST API** receives job submissions
2. **Job Producer** sends jobs to Kafka topic
3. **Job Consumer** processes jobs asynchronously from Kafka
4. **Database** stores job state and results
5. **Status API** provides real-time job monitoring

## Technology Stack

- **Java 17** with **Spring Boot 3.2.0**
- **Apache Kafka** for message queuing
- **PostgreSQL** for production database
- **H2** for local development
- **Flyway** for database migrations
- **Prometheus** for metrics collection
- **Grafana** for monitoring dashboards
- **Docker Compose** for local development environment

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.6+
- Docker and Docker Compose

### Running Locally

1. **Start the infrastructure services**:
   ```bash
   docker-compose up -d
   ```

2. **Initialize Kafka topics**:
   ```bash
   ./docker/init-kafka-topics.sh
   ```

3. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

4. **Access the services**:
   - Application: http://localhost:8081
   - Kafka UI: http://localhost:8080
   - Prometheus: http://localhost:9090
   - Grafana: http://localhost:3000 (admin/admin)
   - H2 Console: http://localhost:8081/h2-console

### Testing the Distributed System

1. **Submit a job via REST API**:
   ```bash
   curl -X POST http://localhost:8081/api/jobs \
     -H "Content-Type: application/json" \
     -d '{"jobType":"test-job","payload":{"message":"Hello World","number":42}}'
   ```

2. **Check job status** (copy the jobId from the response):
   ```bash
   curl http://localhost:8081/api/jobs/{jobId}
   ```

3. **Monitor Kafka processing**:
   - Open Kafka UI at http://localhost:8080
   - Check the `job-queue` topic for messages
   - Watch job status change from PENDING → PROCESSING → COMPLETED

4. **View job statistics**:
   ```bash
   curl http://localhost:8081/api/jobs/stats
   ```

### Running Tests

```bash
mvn test
```

## Project Structure

```
src/
├── main/
│   ├── java/com/example/distributedjobqueue/
│   │   ├── DistributedJobQueueApplication.java
│   │   ├── controller/          # REST API controllers
│   │   ├── service/            # Business logic
│   │   ├── model/              # Domain models
│   │   ├── repository/         # Data access layer
│   │   ├── config/             # Configuration classes
│   │   └── worker/             # Job processing workers
│   └── resources/
│       ├── application.properties
│       ├── application-local.properties
│       ├── application-production.properties
│       └── db/migration/       # Flyway migrations
└── test/                       # Test classes
```

## Configuration

The application uses Spring profiles:

- **local**: Uses H2 in-memory database, suitable for development
- **production**: Uses PostgreSQL with Flyway migrations

## API Endpoints

### Submit a Job
```bash
POST /api/jobs
Content-Type: application/json

{
  "jobType": "email-job",
  "payload": {
    "email": "user@example.com",
    "subject": "Welcome"
  }
}
```

### Get Job Status
```bash
GET /api/jobs/{jobId}
```

### List Jobs
```bash
GET /api/jobs?page=0&size=10&jobType=email-job&status=PENDING
```

### Get Job Statistics
```bash
GET /api/jobs/stats
```

### Cancel a Job
```bash
DELETE /api/jobs/{jobId}
```

## Supported Job Types

- **email-job**: Send emails (simulated)
- **data-processing**: Process data tasks
- **file-processing**: Process files
- **test-job**: Test job processing with custom payload

## Development Workflow

This project follows an incremental development approach with GitHub issues and Pull Requests:

1. **Project & Docker Compose Setup** - Infrastructure and containerization ✅
2. **Application Configuration** - Spring profiles and database setup ✅
3. **REST API Endpoints** - Job submission and status endpoints ✅
4. **Kafka Integration** - Asynchronous job processing with Kafka ✅
5. **Metrics & Monitoring** - Prometheus integration and dashboards

## Contributing

1. Create a feature branch from `main`
2. Make your changes
3. Add tests for new functionality
4. Submit a pull request

## Monitoring

The application exposes comprehensive metrics for monitoring job queue performance:

### Prometheus Metrics Endpoint
```
GET /actuator/prometheus
```

### Custom Job Queue Metrics
```
GET /actuator/job-queue
```

### Available Metrics

#### Queue Metrics
- `job_queue_size` - Current number of jobs in queue (pending + processing)
- `jobs_created_total` - Total number of jobs created
- `jobs_completed_total` - Total number of jobs completed successfully
- `jobs_failed_total` - Total number of jobs that failed
- `jobs_retried_total` - Total number of jobs that were retried

#### Performance Metrics
- `job_processing_duration` - Time taken to process jobs (with percentiles)
- `jobs_processed_by_type_total{job_type="email|data|file|test"}` - Jobs processed by type

#### Custom Endpoint Metrics
The `/actuator/job-queue` endpoint provides:
```json
{
  "queue": {
    "size": 5,
    "pending": 3,
    "processing": 2,
    "completed": 150,
    "failed": 5,
    "total": 158
  },
  "processing": {
    "created": 158,
    "completed": 150,
    "failed": 5,
    "retried": 8
  },
  "performance": {
    "successRate": "94.94%",
    "totalProcessed": 155
  }
}
```

### Grafana Dashboards

The application includes Grafana dashboards for visualizing:

- Queue size over time
- Job processing latency percentiles
- Success/failure rates
- Job type distribution
- System health metrics

### Monitoring Setup

1. **Prometheus**: Metrics collection at `/actuator/prometheus`
2. **Grafana**: Dashboard visualization
3. **Kafka UI**: Message queue monitoring
4. **Spring Actuator**: Application health and custom metrics

## License

This project is licensed under the MIT License.
