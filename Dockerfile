FROM eclipse-temurin:17-jdk-alpine
ARG JAR_FILE=target/*.jar
ADD JAR_FILE conveyor.jar
ENTRYPOINT ["java", "-jar", "/conveyor.jar"]