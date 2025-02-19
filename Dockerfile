# Stage 1: Build the application
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean install

FROM openjdk:17-alpine
WORKDIR /app
COPY --from=build /app/target/lala-house-0.0.1-SNAPSHOT.jar ./lala-house.jar
EXPOSE 8080
CMD ["java", "-jar", "lala-house.jar"]