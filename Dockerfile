FROM openjdk:17-ea-33-jdk-slim-buster

WORKDIR /app
COPY ./target/restaurant-0.0.8.jar /app
COPY ./src/main/resources/ /app/resources/

CMD ["java", "-jar", "restaurant-0.0.8.jar"]
