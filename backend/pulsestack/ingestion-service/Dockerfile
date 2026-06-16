FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY shared-domain/pom.xml shared-domain/
COPY auth-service/pom.xml auth-service/
COPY ingestion-service/pom.xml ingestion-service/
COPY processing-service/pom.xml processing-service/
COPY chat-service/pom.xml chat-service/
RUN mvn dependency:go-offline -pl ingestion-service -am -q
COPY shared-domain/src shared-domain/src
COPY ingestion-service/src ingestion-service/src
RUN mvn package -pl ingestion-service -am -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/ingestion-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-Xmx350m", "-XX:MaxMetaspaceSize=128m", "-XX:+UseSerialGC", "-jar", "app.jar"]
