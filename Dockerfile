# news-aggregator
# Dockerfile
# @May - 2023 - Edoardo Sabatini
# # # # # # # # # # # # # # # # #
FROM ubuntu:latest AS build

RUN apt-get update
RUN apt-get install openjdk-17-jdk -y

RUN mkdir /app
COPY . /app
COPY ApiKey.* /app/src/main/resources/ApiKey.properties
WORKDIR /app

RUN ./gradlew bootJar --no-daemon

FROM openjdk:17-jdk-slim

EXPOSE 8080

COPY --from=build /app/build/libs/newsaggregator-1.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
