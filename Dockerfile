# syntax = docker/dockerfile:1.2

FROM eclipse-temurin:21-jdk-alpine
COPY . .
RUN --mount=type=secret,id=application_yaml,dst=application.yaml cp application.yaml src/main/resources/application.yaml
RUN chmod +x gradlew && ./gradlew build
ENTRYPOINT ["java","-jar","/build/libs/polybacs-0.0.1-SNAPSHOT.jar"]
