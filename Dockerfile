FROM eclipse-temurin:21-jdk-alpine
COPY . .
RUN chmod +x gradlew
RUN ./gradlew build
ENTRYPOINT ["java","-jar","/build/libs/polybacs-0.0.1-SNAPSHOT.jar"]