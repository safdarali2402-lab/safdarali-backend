# Use official Java 17 image
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Give permission to mvnw
RUN chmod +x mvnw

# Build the project
RUN ./mvnw clean package -DskipTests

# Expose port (important)
EXPOSE 8080

# Run Spring Boot jar
CMD ["java", "-jar", "target/*.jar"]
