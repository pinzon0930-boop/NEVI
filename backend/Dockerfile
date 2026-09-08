# ── ETAPA 1: Build con Maven ─────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copia pom.xml primero para aprovechar la caché de capas de Docker.
# Si pom.xml no cambia, Maven no vuelve a descargar las dependencias.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia el código fuente y compila.
COPY src ./src
RUN mvn package -DskipTests -B

# ── ETAPA 2: Imagen final liviana ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copia el JAR generado en la etapa de build.
COPY --from=builder /app/target/*.jar app.jar

# Puerto en el que escucha Spring Boot.
EXPOSE 8080

# Comando de inicio.
ENTRYPOINT ["java", "-jar", "app.jar"]
