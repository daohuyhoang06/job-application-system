# STAGE 1: Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml và tải dependency trước để tận dụng cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy toàn bộ source code
COPY src ./src

# Build ứng dụng
RUN mvn clean package -DskipTests

# ==========================================
# STAGE 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Lấy file jar từ stage 1
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
