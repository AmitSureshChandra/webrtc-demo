# Stage 1: Build JAR
FROM gradle:8.4.0-jdk17-alpine AS builder
WORKDIR /app
COPY . .
RUN gradle clean build --no-daemon

# Stage 2: Run JAR in a minimal image
# FROM eclipse-temurin:17-jre-alpine

FROM eclipse-temurin:17-alpine

WORKDIR /app
COPY --from=builder /app/build/libs/signaling-server-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-Djarmode=layertools", "-jar", "app.jar"]
