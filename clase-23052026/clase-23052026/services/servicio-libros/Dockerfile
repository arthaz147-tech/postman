# =============================================================
# Multi-stage build — Servicio de Libros (Spring Boot)
# =============================================================
# Stage 1: Compilar con Maven
# Stage 2: Ejecutar solo el JAR (imagen más liviana)
#
# CONCEPTO SOA: cada servicio tiene su propio Dockerfile.
# Puede construirse e instalarse en un servidor independiente.
# =============================================================

# Stage 1 — Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Descarga dependencias primero (aprovecha cache de Docker)
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

# Stage 2 — Runtime (imagen liviana sin Maven)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8001
ENTRYPOINT ["java", "-jar", "app.jar"]
