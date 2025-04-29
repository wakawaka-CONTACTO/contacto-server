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
COPY ${SERVICE_NAME}/build/libs/*.jar app.jar
COPY newrelic/ newrelic/
ENV SPRING_PROFILES_ACTIVE=dev

# Stage 3: 최종 이미지 (최소화된 JRE와 애플리케이션 포함)
FROM alpine:3.18.4
ENV JAVA_HOME=/custom-jre
ENV PATH="$JAVA_HOME/bin:$PATH"
WORKDIR /app
COPY --from=builder-jre /custom-jre $JAVA_HOME
COPY --from=builder /app/app.jar app.jar
COPY --from=builder /app/newrelic/ newrelic/

# 운영 여부를 구분할 플래그 추가
ENV USE_NEW_RELIC=false

# 런타임에 선택적으로 New Relic Agent 활성화
ENTRYPOINT ["/bin/sh", "-c", "\
    if [ \"$USE_NEW_RELIC\" = \"true\" ]; then \
      java -javaagent:/app/newrelic/newrelic.jar \
        -Dnewrelic.config.app_name=$NEW_RELIC_APP_NAME \
        -Dnewrelic.config.license_key=$NEW_RELIC_LICENSE_KEY \
        -Duser.timezone=Asia/Seoul \
        -Dspring.profiles.active=dev \
        -jar app.jar; \
    else \
      java \
        -Duser.timezone=Asia/Seoul \
        -Dspring.profiles.active=dev \
        -jar app.jar; \
    fi"]