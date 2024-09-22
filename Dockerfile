# syntax = docker/dockerfile:1.10

FROM eclipse-temurin:21.0.4_7-jdk-alpine
RUN apk add --update npm
COPY . .
RUN --mount=type=secret,id=application_yaml,dst=application.yaml cp application.yaml src/main/resources/application.yaml
RUN cd frontend && npm ci && npm run build
RUN chmod +x gradlew && ./gradlew -PcopyFrontend build

FROM eclipse-temurin:21.0.4_7-jre-alpine
COPY --from=0 /build/libs/polybacs-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
