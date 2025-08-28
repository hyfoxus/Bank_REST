# ---------- build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
# быстрее: пропусти тесты в контейнере; локально ты их уже гоняешь
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests package

# ---------- run ----------
FROM eclipse-temurin:21-jre-alpine
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"
ENV TZ=UTC
WORKDIR /opt/app
COPY --from=build /app/target/*.jar app.jar

# non-root user
RUN addgroup -S app && adduser -S app -G app
USER app

EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]