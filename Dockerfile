FROM gradle:8.13-jdk21-alpine AS gradle-builder
WORKDIR /app

COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle build

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=gradle-builder /app/build/libs/findmyflight-0.0.1-SNAPSHOT.jar find-my-flight.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "find-my-flight.jar"]

