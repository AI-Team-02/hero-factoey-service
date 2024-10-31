# Build stage
FROM gradle:jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon

# Run stage
FROM eclipse-temurin:17-jre-focal
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

# Time zone setting
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

EXPOSE 8081
ENTRYPOINT java -jar -Dspring.profiles.active=secret -Duser.timezone=Asia/Seoul -Dfile.encoding=UTF-8 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/logs/heapdump.hprof -Xms512m -Xmx1024m app.jar
