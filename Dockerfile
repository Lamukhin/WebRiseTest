FROM gradle:jdk21 as build
USER root
WORKDIR /workspace/app
ADD /secret_information.env /workspace
COPY build.gradle settings.gradle gradlew ./
COPY gradle/ gradle/
RUN chmod +x gradle --version

COPY . .
RUN chmod +x gradle && gradle build -x test

FROM eclipse-temurin:17-alpine
WORKDIR /workspace/app
COPY --from=build /workspace/app/build/libs/*.jar app.jar

CMD ["java","-jar","app.jar"]