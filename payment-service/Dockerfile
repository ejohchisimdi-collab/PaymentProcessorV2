# payment-service/Dockerfile
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

# Build payment-events first
COPY payment-events ./payment-events
WORKDIR /app/payment-events
RUN mvn clean install -DskipTests

# Build this service
WORKDIR /app/payment-service
COPY payment-service/pom.xml .
RUN mvn dependency:resolve -B
COPY payment-service/src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17.0.13_11-jre-jammy
WORKDIR /app
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /app/payment-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
