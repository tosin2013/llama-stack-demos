# Dockerfile for Workshop Monitoring Service with Evolution Engine
FROM registry.access.redhat.com/ubi8/openjdk-17-runtime:1.20

# Set environment variables
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV AB_ENABLED=jmx_exporter

# Set working directory
WORKDIR /deployments

# Copy the uber jar (built by Maven)
COPY target/workshop-monitoring-service-1.0.0-SNAPSHOT-runner.jar app.jar

# Expose ports
EXPOSE 8080 8443

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
