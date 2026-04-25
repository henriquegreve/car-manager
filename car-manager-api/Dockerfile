# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /build
COPY . .
RUN chmod +x mvnw \
    && ./mvnw -q -DskipTests package \
    && find target -maxdepth 1 -name 'car-manager-api-*.jar' ! -name '*-plain.jar' \
         -exec cp {} /build/application.jar \;

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=build --chown=spring:spring /build/application.jar /app/application.jar
USER spring:spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/application.jar"]
