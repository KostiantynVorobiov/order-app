FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

RUN ./mvnw dependency:go-offline

COPY src ./src

RUN ./mvnw install -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /tmp

COPY --from=builder /app/target/orderapp-0.0.1-SNAPSHOT.jar orderapp.jar

EXPOSE 8080

COPY src/main/resources/application-docker.properties /config/application.properties

CMD ["java", "-jar", "orderapp.jar", "--spring.config.location=/config/application.properties"]

## Копіюємо скрипт-обгортку з вашої директорії "E:\IdeaProjects\orderapp\orderapp"
## до контейнера у директорію /usr/local/bin/
#COPY entrypoint.sh /usr/local/bin/entrypoint.sh
#
## Надаємо права на виконання скрипту
#RUN chmod +x /usr/local/bin/entrypoint.sh
#
## Встановлюємо скрипт як основну точку входу (entrypoint) для контейнера.
## Це гарантує, що скрипт буде запускатися першим при старті контейнера.
#ENTRYPOINT ["entrypoint.sh"]
