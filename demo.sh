#!/bin/bash

# Distributed Job Queue - Local Demo Script
# This script demonstrates the complete Kafka-integrated job queue workflow

echo "🚀 Distributed Job Queue - Local Demo"
echo "======================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker first."
    exit 1
fi

# Check if Kafka is already running
if docker-compose ps | grep -q "kafka.*Up"; then
    print_warning "Kafka is already running. Skipping infrastructure startup."
else
    print_status "Starting Kafka infrastructure..."
    docker-compose up -d

    print_status "Waiting for Kafka to be ready..."
    sleep 30

    # Verify Kafka is running
    if docker-compose ps | grep -q "kafka.*Up"; then
        print_success "Kafka infrastructure started successfully!"
    else
        print_error "Failed to start Kafka infrastructure"
        exit 1
    fi
fi

# Check if application is already running
if lsof -Pi :8081 -sTCP:LISTEN -t >/dev/null ; then
    print_warning "Application is already running on port 8081"
else

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"
    APP_PID=$!

    print_status "Waiting for application to start..."
    sleep 20

    # Verify application is running
    if lsof -Pi :8081 -sTCP:LISTEN -t >/dev/null ; then
        print_success "Application started successfully on port 8081!"
    else
        print_error "Failed to start application"
        cat app.log
        exit 1
    fi
fi

echo ""
print_success "🎉 System is ready! Here's what you can do:"
echo ""
echo "1. 🌐 Access points:"
echo "   - Application: http://localhost:8081"
echo "   - Kafka UI:     http://localhost:8080"
echo "   - H2 Console:   http://localhost:8081/h2-console"
echo ""
echo "2. 🧪 Test the workflow:"
echo "   # Submit a job"
echo "   curl -X POST http://localhost:8081/api/jobs \\"
echo "     -H \"Content-Type: application/json\" \\"
echo "     -d '{\"jobType\":\"test-job\",\"payload\":{\"message\":\"Hello World\",\"number\":42}}'"
echo ""
echo "   # Check job status (replace JOB_ID with the returned ID)"
echo "   curl http://localhost:8081/api/jobs/JOB_ID"
echo ""
echo "   # View job statistics"
echo "   curl http://localhost:8081/api/jobs/stats"
echo ""
echo "3. 📊 Monitor:"
echo "   - Open Kafka UI to see messages in the 'job-queue' topic"
echo "   - Watch application logs for job processing"
echo "   - Check H2 console for database state"
echo ""
echo "4. 🛑 To stop everything:"
echo "   docker-compose down"
echo "   kill $APP_PID"
echo ""

print_status "Demo setup complete! The distributed job queue is now running with Kafka integration."
