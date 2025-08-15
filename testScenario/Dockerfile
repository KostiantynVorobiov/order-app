FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

RUN ./mvnw dependency:go-offline

COPY src ./src

RUN ./mvnw install -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /tmp

COPY --from=builder /app/target/testScenario-0.0.1-SNAPSHOT.jar testScenario.jar

EXPOSE 8081

CMD ["java", "-jar", "testScenario.jar"]
