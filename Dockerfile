# Use a lightweight JDK image
FROM eclipse-temurin:21-jdk-alpine

# Set the working directory
WORKDIR /app

# Copy jar file into container
COPY target/*.jar app.jar

# Expose port (default Spring Boot port)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
