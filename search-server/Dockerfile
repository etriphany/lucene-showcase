FROM        openjdk:12-jdk-alpine

# Spring Boot Cookbook
ARG         JAR_FILE
COPY        ${JAR_FILE} app.jar
ENTRYPOINT  ["java", "-jar", "/app.jar"]
