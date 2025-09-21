# Use Java 21
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy all source code
COPY . .

# Build the app inside Docker
RUN ./mvnw clean package -DskipTests

# Copy the built jar
RUN cp target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
