# 공통 베이스 이미지 설정
FROM amd64/amazoncorretto:21-alpine-jdk AS base
WORKDIR /app

# 공통 빌드 인스트럭션
FROM base AS builder
ARG SERVICE_NAME

COPY ${SERVICE_NAME}/build/libs/*.jar app.jar

# 최종 이미지 설정
FROM amd64/amazoncorretto:21-alpine-jdk
COPY --from=builder /app/app.jar app.jar
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "-Dspring.profiles.active=dev", "app.jar"]
