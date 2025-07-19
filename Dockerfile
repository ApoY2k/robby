FROM openjdk:slim as final

WORKDIR /app

COPY assets assets
COPY build/libs/robby-all.jar robby.jar

CMD java -jar robby.jar
