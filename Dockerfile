FROM openjdk:17-jdk-alpine
WORKDIR /movie

COPY ./build/libs/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
