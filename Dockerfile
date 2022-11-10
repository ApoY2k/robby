FROM maven as build

WORKDIR /app

COPY src src
COPY pom.xml pom.xml

RUN ["mvn", "-B", "-Dmaven.test.skip=true", "package"]

FROM openjdk:slim as final

WORKDIR /app

COPY --from=build /app/target .

ENV DATABASE_PATH="robby.db"
ENV JAVA_OPTS=""

CMD java $JAVA_OPTS -cp classes:dependency/* apoy2k.robby.ApplicationKt
