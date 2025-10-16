# syntax = docker/dockerfile:1.19

FROM eclipse-temurin:21.0.5_11-jdk-alpine
RUN apk upgrade
RUN apk add --update npm
WORKDIR /app
COPY . .
RUN --mount=type=secret,id=application_yaml,dst=application.yaml cp application.yaml src/main/resources/application.yaml
RUN cd frontend && npm ci && npm run build
RUN chmod +x gradlew && ./gradlew -PcopyFrontend build

FROM eclipse-temurin:21.0.5_11-jre-alpine
WORKDIR /app
COPY --from=0 /app/build/libs/polybacs-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]