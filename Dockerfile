# Etapa 1: Compilar el SPI con Maven + JDK 21
FROM maven:3.9.8-eclipse-temurin-21 AS spi_builder
WORKDIR /build
COPY pom.xml .                     
COPY src/ ./src/                   
RUN mvn clean package              

# Etapa 2: Incorporar el JAR en Keycloak y construir
FROM quay.io/keycloak/keycloak:26.1.0 AS builder
USER root
COPY --from=spi_builder \
    /build/target/keycloak-json-spi-1.0-SNAPSHOT.jar \
    /opt/keycloak/providers/
RUN chown keycloak:keycloak \
    /opt/keycloak/providers/keycloak-json-spi-1.0-SNAPSHOT.jar
USER keycloak
RUN /opt/keycloak/bin/kc.sh build --db=postgres

# Etapa 3: Imagen final de runtime de Keycloak
FROM quay.io/keycloak/keycloak:26.1.0
COPY --from=builder /opt/keycloak/ /opt/keycloak/
ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
CMD ["start"]
