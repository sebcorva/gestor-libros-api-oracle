# ---------- Etapa 1: compilar ----------
# Imagen con Maven + JDK 17 (solo se usa para construir, no llega a la imagen final)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Primero solo el pom: si no cambia, Docker reutiliza la capa con las dependencias descargadas
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Luego el codigo fuente y empaquetado (los tests ya se ejecutan en CI)
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Etapa 2: ejecutar ----------
# Solo el JRE (mas liviano que el JDK)
FROM eclipse-temurin:17-jre
WORKDIR /app

# unzip: lo usa el entrypoint para extraer el wallet
# Usuario sin privilegios + carpeta del wallet donde ese usuario puede escribir
RUN apt-get update \
    && apt-get install -y --no-install-recommends unzip \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --no-create-home --uid 1001 spring \
    && mkdir -p /app/wallet \
    && chown spring:spring /app/wallet

COPY --from=build /app/target/*.jar /app/app.jar
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
# sed: quita CRLF por si el script se edita en Windows; 0555 = solo lectura y ejecucion
RUN sed -i 's/\r$//' /app/docker-entrypoint.sh && chmod 0555 /app/docker-entrypoint.sh

# El wallet NO va en la imagen: el entrypoint lo extrae aqui al arrancar
ENV TNS_ADMIN=/app/wallet

USER spring
EXPOSE 8080
ENTRYPOINT ["/app/docker-entrypoint.sh"]
