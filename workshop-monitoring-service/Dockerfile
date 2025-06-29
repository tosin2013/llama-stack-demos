# Multi-stage Dockerfile for Workshop Monitoring Service with Evolution Engine

# Build stage
FROM registry.access.redhat.com/ubi8/openjdk-17:1.20 AS builder

# Set working directory
WORKDIR /app

# Install Maven and copy pom.xml first for better caching
RUN microdnf install -y maven && microdnf clean all

# Copy pom.xml first for better caching
COPY pom.xml ./

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -Dquarkus.package.type=uber-jar

# Runtime stage
FROM registry.access.redhat.com/ubi8/openjdk-17-runtime:1.20

# Set environment variables
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV AB_ENABLED=jmx_exporter

# Set working directory
WORKDIR /deployments

# Copy the uber jar from builder stage
COPY --from=builder /app/target/*-runner.jar app.jar

# Expose ports
EXPOSE 8080 8443

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
