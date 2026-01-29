# webhook-service/Dockerfile
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

# Build payment-events first (if needed)
COPY payment-events ./payment-events
WORKDIR /app/payment-events
RUN mvn clean install -DskipTests

# Build webhook-service
WORKDIR /app/webhook-service
COPY webhook-service/pom.xml .
RUN mvn dependency:resolve -B
COPY webhook-service/src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17.0.13_11-jre-jammy
WORKDIR /app
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /app/webhook-service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
