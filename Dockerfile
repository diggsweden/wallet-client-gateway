# SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
# SPDX-License-Identifier: EUPL-1.2

# Multi-stage Dockerfile for EUDIW Client Gateway

# Stage 1: Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder

LABEL maintainer="Digg - Agency for Digital Government"
LABEL description="Build stage for EUDIW Client Gateway"

# Install dependencies needed for building
RUN apk add --no-cache curl

# Create app directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Make mvnw executable
RUN chmod +x ./mvnw

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests -B && \
    java -Djarmode=layertools -jar target/*.jar extract

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine AS runtime

LABEL maintainer="Digg - Agency for Digital Government"
LABEL description="EUDIW Client Gateway - European Digital Identity Wallet Client Gateway"
LABEL version="0.1.0-SNAPSHOT"

# Install runtime dependencies
RUN apk add --no-cache \
    curl \
    ca-certificates \
    tzdata && \
    rm -rf /var/cache/apk/*

# Create non-root user
RUN addgroup -g 1000 appuser && \
    adduser -D -u 1000 -G appuser appuser

# Create app directory
WORKDIR /app

# Copy Spring Boot layers from builder stage
COPY --from=builder --chown=appuser:appuser /app/dependencies/ ./
COPY --from=builder --chown=appuser:appuser /app/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appuser /app/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appuser /app/application/ ./

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Set JVM options for container environment
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -Djava.security.egd=file:/dev/./urandom \
    -Dfile.encoding=UTF-8"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]