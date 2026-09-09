FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY shared-domain/pom.xml shared-domain/
COPY auth-service/pom.xml auth-service/
COPY ingestion-service/pom.xml ingestion-service/
COPY processing-service/pom.xml processing-service/
COPY chat-service/pom.xml chat-service/
COPY pulsestack-app/pom.xml pulsestack-app/
RUN mvn dependency:go-offline -pl pulsestack-app -am -q

COPY shared-domain/src shared-domain/src
COPY auth-service/src auth-service/src
COPY ingestion-service/src ingestion-service/src
COPY processing-service/src processing-service/src
COPY chat-service/src chat-service/src
COPY pulsestack-app/src pulsestack-app/src
RUN mvn package -pl pulsestack-app -am -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/pulsestack-app/target/pulsestack-app-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:InitialRAMPercentage=20.0", "-XX:MaxRAMPercentage=65.0", "-XX:MaxMetaspaceSize=256m", "-XX:+UseSerialGC", "-jar", "app.jar"]
