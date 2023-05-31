FROM amazoncorretto:17

ARG JAR_FILE=target/TelegramBotApi-0.0.1-SNAPSHOT.jar

WORKDIR /app

COPY ${JAR_FILE} app.jar

EXPOSE 8099

ENTRYPOINT ["java","-jar","app.jar"]