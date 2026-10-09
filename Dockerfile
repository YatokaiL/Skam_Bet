FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

# dependencies first, so they are cached between builds
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null

COPY src src
RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/build/libs/skam-bet.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
