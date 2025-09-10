# Distributed Job Queue

A distributed job queue built in Java using Spring Boot, showcasing concurrency, worker pools, and reliable task execution. Implements retries, scheduling, and monitoring to demonstrate scalable system design principles.

## Features

- **Distributed Architecture**: Uses Apache Kafka for message queuing
- **Reliable Processing**: Implements retry mechanisms and error handling
- **Monitoring**: Prometheus metrics and Grafana dashboards
- **Scalable**: Worker pools and concurrent processing
- **Database Support**: PostgreSQL for production, H2 for local development
- **REST API**: Simple HTTP endpoints for job submission and status checking

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

## Development Workflow

This project follows an incremental development approach with GitHub issues and Pull Requests:

1. **Project & Docker Compose Setup** - Infrastructure and containerization
2. **Application Configuration** - Spring profiles and database setup
3. **REST API Endpoints** - Job submission and status endpoints
4. **Metrics & Monitoring** - Prometheus integration and dashboards

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
