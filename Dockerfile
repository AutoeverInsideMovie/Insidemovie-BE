FROM openjdk:17-jdk-alpine
WORKDIR /movie

COPY ./build/libs/*.jar app.jar
# COPY build/libs/application-API-KEY.properties application-API-KEY.properties

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]