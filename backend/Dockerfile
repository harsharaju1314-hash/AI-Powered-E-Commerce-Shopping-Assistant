# ==============================================================================
# Multi-Stage Dockerfile for Spring Boot 3 Backend
# Stage 1: Build Application with Maven & Temurin JDK 17
# Stage 2: Minimal Distroless / Temurin JRE 17 Production Image
# ==============================================================================

FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /workspace

# Copy Maven Wrapper and configuration files
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B || true

# Copy source code and build production jar
COPY src src
RUN ./mvnw clean package -DskipTests -B

# Extract layered Spring Boot jars for Docker layer caching
WORKDIR /workspace/target/extracted
RUN java -Djarmode=layertools -jar /workspace/target/ai-shopping-assistant-backend-1.0.0-SNAPSHOT.jar extract

# ------------------------------------------------------------------------------
# Stage 2: Lightweight Production Runtime Image
# ------------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="harsharaju1314-hash" \
      application="AI-Powered E-Commerce Shopping Assistant Backend" \
      version="1.0.0"

# Install curl for container health check
RUN apk add --no-cache curl tzdata && \
    addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy layered Spring Boot components from builder stage
COPY --from=builder /workspace/target/extracted/dependencies/ ./
COPY --from=builder /workspace/target/extracted/spring-boot-loader/ ./
COPY --from=builder /workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=builder /workspace/target/extracted/application/ ./

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080

# Configure container-aware JVM flags
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
