FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/api-gateway-*.jar app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]