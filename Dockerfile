# ─────────────────────────────────────────────
# Etapa 1: Build con Maven
# ─────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copiar pom.xml primero para aprovechar el caché de capas
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente y compilar (sin tests)
COPY src ./src
RUN mvn clean package -DskipTests -B

# ─────────────────────────────────────────────
# Etapa 2: Imagen final liviana
# ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar el JAR generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Puerto que expone la aplicación
EXPOSE 8080

# Variables de entorno por defecto (se sobreescriben con docker-compose)
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/reservas2025 \
    SPRING_DATASOURCE_USERNAME=postgres \
    SPRING_DATASOURCE_PASSWORD=Colombia2020*

ENTRYPOINT ["java", "-jar", "app.jar"]
