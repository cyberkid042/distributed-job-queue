#!/bin/bash

# Wait for Kafka to be ready
echo "Waiting for Kafka to be ready..."
until docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list > /dev/null 2>&1; do
  echo "Kafka is not ready yet. Waiting..."
  sleep 2
done

echo "Kafka is ready. Creating topics..."

# Create topics
docker exec kafka kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --replication-factor 1 \
  --partitions 3 \
  --topic jobs

docker exec kafka kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --replication-factor 1 \
  --partitions 3 \
  --topic job-results

docker exec kafka kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --replication-factor 1 \
  --partitions 1 \
  --topic job-status

echo "Topics created successfully!"

# List topics to verify
echo "Current topics:"
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list
