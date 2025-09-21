FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy all source files
COPY . .

# Give permission to Maven wrapper
RUN chmod +x mvnw

# Build the app
RUN ./mvnw clean package -DskipTests

# Copy the jar to a fixed name
RUN cp target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
