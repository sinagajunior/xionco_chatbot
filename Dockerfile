# Build stage
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /build

# Copy source and build files
COPY pom.xml .
COPY src src/

# Build application
RUN apt-get update && apt-get install -y maven && \
    mvn clean package -DskipTests && \
    cp target/xionco-chatbot-1.0.0.jar app.jar

# Runtime stage
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copy jar from builder
COPY --from=builder /build/app.jar .

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
