# Use official Java 17 image
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Install Maven (since we don't have mvnw)
RUN apk add --no-cache maven

# Copy project files
COPY . .

# Build the project (skip tests for faster builds)
RUN mvn clean package -DskipTests

# Expose port (important for container runtime)
EXPOSE 8080

# Run Spring Boot JAR file
CMD ["java", "-jar", "target/*.jar"]
