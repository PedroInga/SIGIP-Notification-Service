# syntax=docker/dockerfile:1
# Imagen de notification-service (repositorio SIGIP-Notification-Service).
# Necesita el código de SIGIP-Common como contexto adicional llamado "common":
#   docker build --build-context common=../SIGIP-Common -t sigip/notification-service .
# docker-compose (carpeta SIGIP) ya lo pasa con "additional_contexts".
# Etapa 1: instala SIGIP-Common y compila el servicio con Maven. Etapa 2: solo el JRE y el .jar.

FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /common
COPY --from=common pom.xml .
COPY --from=common src src
WORKDIR /src
COPY pom.xml .
COPY src src
# La caché de ~/.m2 evita volver a descargar dependencias en cada build
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -f /common/pom.xml install -DskipTests \
    && mvn -B -q package -DskipTests \
    && cp target/notification-service.jar /app.jar

FROM eclipse-temurin:25-jre
RUN useradd --system --uid 1001 sigip
USER sigip
WORKDIR /app
COPY --from=build /app.jar app.jar
# Ajusta la memoria de la JVM al límite del contenedor
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
