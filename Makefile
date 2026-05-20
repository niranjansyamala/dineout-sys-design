# Build Docker image
docker-build:
	docker build -t dineout-app:latest .

# Run application with Docker Compose
docker-up:
	docker-compose up -d

# Stop Docker Compose
docker-down:
	docker-compose down

# View logs
docker-logs:
	docker-compose logs -f app

# Clean up everything
docker-clean:
	docker-compose down -v
	docker rmi dineout-app:latest

# Build with Maven
build:
	mvn clean package -DskipTests

# Run tests
test:
	mvn test

# Run application locally
run:
	mvn spring-boot:run

.PHONY: docker-build docker-up docker-down docker-logs docker-clean build test run
