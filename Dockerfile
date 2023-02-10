FROM eclipse-temurin:17-jdk-alpine
ADD target/*.jar conveyor.jar
ENTRYPOINT ["java", "-jar", "/conveyor.jar"]
