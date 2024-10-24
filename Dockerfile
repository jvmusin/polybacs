# syntax = docker/dockerfile:1.10

FROM eclipse-temurin:21.0.4_7-jdk-alpine
RUN apk add --update npm
WORKDIR /app
COPY . .
RUN --mount=type=secret,id=ACCESS_KEYS,dst=ACCESS_KEYS.yaml cp ACCESS_KEYS.yaml src/main/resources/application.yaml
RUN cd frontend && npm ci && npm run build
RUN chmod +x gradlew && ./gradlew -PcopyFrontend build

FROM eclipse-temurin:21.0.4_7-jre-alpine
WORKDIR /app
COPY --from=0 /app/build/libs/polybacs-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]