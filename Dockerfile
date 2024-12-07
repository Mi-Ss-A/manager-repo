# 1. Gradle 빌드 이미지 (빌드 단계)
FROM gradle:7.5.1-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle clean build -x test

# 2. 실행 이미지 (최종 배포 단계)
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/build/libs/missa-0.0.1-SNAPSHOT.jar app.jar

# 포트를 8086으로 변경
EXPOSE 8086

# 애플리케이션 실행 명령
ENTRYPOINT ["java", "-jar", "app.jar"]
