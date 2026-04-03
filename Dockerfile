# ── Stage 1: Build ──────────────────────────────────────────────────────────
# Dùng Maven + JDK 17 để compile và đóng gói JAR
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml trước để cache layer dependencies
# Nếu pom.xml không đổi, Docker dùng cache → không download lại
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build JAR, bỏ qua test (test chạy ở CI pipeline)
RUN mvn clean package -DskipTests

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
# Chỉ cần JRE để chạy, không cần JDK đầy đủ → image nhỏ hơn ~3x
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Tạo thư mục lưu file upload (ảnh phòng)
RUN mkdir -p uploads

# Copy JAR từ stage builder
COPY --from=builder /app/target/*.jar app.jar

# Port backend (khớp server.port=8081 trong application.properties)
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
