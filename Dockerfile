# ============================
# 1. Build React Frontend
# ============================
FROM node:18 AS frontend-build
WORKDIR /app/frontend

# Copy React project
COPY frontend/package*.json ./
RUN npm install

COPY frontend/ ./
RUN npm run build -- --outDir dist


# ============================
# 2. Build Spring Boot Backend
# ============================
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app

# Copy backend code
COPY pom.xml .
COPY src ./src

# Copy frontend build → Spring Boot static folder
RUN mkdir -p src/main/resources/static
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static

# Build Spring Boot app (JAR)
RUN mvn clean package -DskipTests


# ============================
# 3. Final Runtime Image
# ============================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy jar from previous stage
COPY --from=backend-build /app/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
