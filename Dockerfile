# Stage 1: Build fat JAR
FROM maven:3.9.6-eclipse-temurin-11 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run JAR
FROM eclipse-temurin:11-jre

WORKDIR /app
RUN mkdir -p /app/tmp && chmod -R 777 /app/tmp
ENV TMPDIR=/app/tmp
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENV TMPDIR=/tmp
ENTRYPOINT ["java", "-Djava.io.tmpdir=/tmp", "-jar", "/app/app.jar"]
