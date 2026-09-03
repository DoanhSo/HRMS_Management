# ==========================================
# Stage 1: Build Stage
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper & POM
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Convert line endings in case of Windows CRLF and grant execution permission
RUN tr -d '\r' < mvnw > mvnw_unix && mv mvnw_unix mvnw && chmod +x mvnw

# Download dependencies (Docker layer caching)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build JAR package without running tests (tests already verified)
RUN ./mvnw clean package -DskipTests

# ==========================================
# Stage 2: Lightweight Runtime Stage
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install curl for healthcheck & tzdata for correct Vietnam timezone
RUN apk add --no-cache curl tzdata && \
    cp /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime && \
    echo "Asia/Ho_Chi_Minh" > /etc/timezone

# Create non-root user
RUN addgroup -S hrms && adduser -S hrms -G hrms
USER hrms:hrms

# Copy generated JAR from builder stage
COPY --from=builder --chown=hrms:hrms /app/target/*.jar app.jar

ENV TZ=Asia/Ho_Chi_Minh \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 0

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
