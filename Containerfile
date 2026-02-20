# SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
# SPDX-License-Identifier: EUPL-1.2

# Stage 1: Build stage
FROM docker.io/library/eclipse-temurin:25-jdk-alpine@sha256:7ace075f29555df6696750ee3caffea4fb542a1db8a5b1e7578129162f071c03 AS builder

LABEL maintainer="Digg - Agency for Digital Government"
LABEL description="Build stage for Wallet Client Gateway"

# Install dependencies needed for building
# hadolint ignore=DL3018
RUN apk add --no-cache curl

# Create app directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Make mvnw executable and download dependencies (cached if pom.xml doesn't change)
RUN chmod +x ./mvnw && \
    ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip checkstyle in Docker build)
RUN ./mvnw clean package -DskipTests -Dcheckstyle.skip=true -B && \
    java -Djarmode=layertools -jar target/*.jar extract

# Stage 2: Runtime stage
FROM cgr.dev/chainguard/jre:latest@sha256:c43291a2bdaff7cf26070e293a929ff36ef993d7240dee8eecc6ceb8497d16cf AS runtime

LABEL maintainer="Digg - Agency for Digital Government"
LABEL description="Wallet Client Gateway"

WORKDIR /app

# Copy Spring Boot layers from builder stage (nonroot user: 65532)
COPY --from=builder --chown=65532:65532 /app/dependencies/ ./
COPY --from=builder --chown=65532:65532 /app/spring-boot-loader/ ./
COPY --from=builder --chown=65532:65532 /app/snapshot-dependencies/ ./
COPY --from=builder --chown=65532:65532 /app/application/ ./

EXPOSE 8080

# JVM options are set directly in ENTRYPOINT since distroless has no shell
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-XX:+UseG1GC", "-XX:+UseStringDeduplication", "-Djava.security.egd=file:/dev/./urandom", "-Dfile.encoding=UTF-8", "org.springframework.boot.loader.launch.JarLauncher"]