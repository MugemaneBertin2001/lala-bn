# Stage 1: Build the application
FROM maven:3.8.4-openjdk-17 AS build

# Set working directory
WORKDIR /app

# Copy the entire project
COPY . .

# Build the application
RUN mvn clean package -DskipTests

# List contents of target directory for debugging
RUN ls -la target/

# Stage 2: Runtime
FROM openjdk:17-alpine

WORKDIR /app

# Copy the jar file from build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]