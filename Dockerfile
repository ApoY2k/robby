FROM maven as build

WORKDIR /app

COPY src src
COPY pom.xml pom.xml

RUN ["mvn", "-B", "-Dmaven.test.skip=true", "package"]

FROM openjdk:slim as final

WORKDIR /app

COPY assets ./assets
COPY --from=build /app/target ./target

CMD java -cp target/classes:target/dependency/* apoy2k.robby.ApplicationKt
