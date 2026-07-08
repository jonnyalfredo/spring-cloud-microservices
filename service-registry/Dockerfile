FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn -DskipTests package

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /build/target/serviceregistry-*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java","-jar","/app/app.jar"]
