FROM gradle:4.7.0-jdk8-alpine AS build

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon 

FROM openjdk:8-jre-slim

# Defined Args
ENV USERNAME=michaelwasher
ENV MULTICAST_IP=239.0.202.1
ENV PORT_NUMBER=40202

EXPOSE 8080
RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/chatclient.jar

ENTRYPOINT java -jar /app/chatclient.jar $USERNAME $MULTICAST_IP $PORT_NUMBER   
