FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY build/libs/gateway-service-0.0.1.jar app.jar

ENV SPRING_PROFILES_ACTIVE=dev

EXPOSE 8087

ENTRYPOINT ["java", "-jar", "app.jar"] 