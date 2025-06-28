FROM registry.access.redhat.com/ubi8/python-39:latest

USER root

# Install system dependencies
RUN dnf update -y && \
    dnf install -y git curl && \
    dnf clean all

USER 1001

# Set working directory
WORKDIR /opt/app-root/src

# Copy requirements first for better caching
COPY requirements.txt .

# Install Python dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy application code
COPY demos/ ./demos/
COPY *.py ./

# Create necessary directories
RUN mkdir -p config logs

# Expose agent port
EXPOSE 8080

# Set environment variables
ENV PYTHONPATH=/opt/app-root/src
ENV AGENT_PORT=8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/agent-card || exit 1

# Start agent based on environment variable
CMD ["python", "-m", "demos.workshop_template_system"]
