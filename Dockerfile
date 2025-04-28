# Stage 1: custom JRE 생성 (jlink 활용)
FROM amazoncorretto:21-alpine-jdk AS builder-jre
RUN apk add --no-cache binutils
RUN $JAVA_HOME/bin/jlink \
    --module-path "$JAVA_HOME/jmods" \
    --add-modules ALL-MODULE-PATH \
    --strip-debug \
    --no-man-pages \
    --compress=2 \
    --output /custom-jre

# Stage 2: 애플리케이션 jar 파일 준비
FROM amazoncorretto:21-alpine-jdk AS builder
ARG SERVICE_NAME
WORKDIR /app
RUN echo ${SERVICE_NAME}
COPY ${SERVICE_NAME}/build/libs/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=dev

# Stage 3: 최종 이미지 (최소화된 JRE와 애플리케이션 포함)
FROM alpine:3.18.4
ENV JAVA_HOME=/custom-jre
ENV PATH="$JAVA_HOME/bin:$PATH"
WORKDIR /app
COPY --from=builder-jre /custom-jre $JAVA_HOME
COPY --from=builder /app/app.jar app.jar

# Datadog Java Agent 다운로드
RUN wget -O dd-java-agent.jar https://dtdg.co/latest-java-tracer

ENTRYPOINT ["java", "-javaagent:/app/dd-java-agent.jar", "-Duser.timezone=Asia/Seoul", "-jar", "-Dspring.profiles.active=dev", "app.jar"]
