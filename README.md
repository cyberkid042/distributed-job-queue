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

The application exposes Prometheus metrics at `/actuator/prometheus` including:

- Queue size metrics
- Job processing latency
- Success/failure counts
- System health metrics

## License

This project is licensed under the MIT License.
