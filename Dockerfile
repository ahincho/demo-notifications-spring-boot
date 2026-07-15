# ============================================
# Nova Platform — demo-notifications-spring-boot
# Multi-stage Dockerfile for the Spring Boot demo.
#
# Build (from this directory):
#   docker buildx build \
#     --build-context hostm2=$env:USERPROFILE\.m2\repository \
#     -t demo-notifications-spring-boot:1.0.0-SNAPSHOT .
#
# The `hostm2` build context injects the locally-published Nova libraries
# (core + spring-boot-starter) into the build container's Maven local repo.
# Prerequisite (one-time, and after any rebuild of the Nova core or starter):
#   cd ..\..\java\nova-java-notifications
#   .\mvnw.cmd install
#   cd ..\..\java\nova-java-notifications-spring-boot-starter
#   .\gradlew.bat publishToMavenLocal
# Then build from this directory with the command shown above.
# ============================================

# syntax=docker/dockerfile:1.7

# ==== Builder ====
FROM eclipse-temurin:25-jdk-alpine AS builder

ARG NOVA_UID=1001
RUN addgroup -S -g $NOVA_UID nova \
 && adduser -S -u $NOVA_UID -G nova nova

WORKDIR /build
RUN chown -R nova:nova /build

# Copy build configuration first (Docker layer cache: deps only rebuild when
# build.gradle.kts / gradle.properties / wrapper change).
COPY --chown=nova:nova gradlew gradlew.bat settings.gradle.kts build.gradle.kts gradle.properties ./
COPY --chown=nova:nova gradle ./gradle
USER nova

# Inject Nova libraries from the host's local Maven repo via the named build
# context. Without this step, Gradle cannot resolve the local starter and the
# build fails. The host path is supplied at build time via --build-context.
COPY --from=hostm2 --chown=nova:nova pe/edu/nova /root/.m2/repository/pe/edu/nova

# Pre-warm dependency resolution (the actual build below also reuses these).
RUN --mount=type=cache,target=/home/nova/.gradle/caches,uid=1001,gid=1001 \
    ./gradlew dependencies --no-daemon || true

# Copy the source and produce the Spring Boot fat jar.
COPY --chown=nova:nova src ./src
RUN --mount=type=cache,target=/home/nova/.gradle/caches,uid=1001,gid=1001 \
    ./gradlew bootJar -x test --no-daemon

# ==== Runtime ====
FROM eclipse-temurin:25-jre-alpine AS runtime

# tini: proper signal handling (PID 1 problem in containers).
# netcat-openbsd: TCP healthcheck (the demo has no /actuator/health endpoint).
RUN apk add --no-cache tini netcat-openbsd

ARG NOVA_UID=1001
RUN addgroup -S -g $NOVA_UID nova \
 && adduser -S -u $NOVA_UID -G nova nova

# OCI + Nova traceability labels.
ARG GIT_SHA=unknown
ARG BUILD_NUMBER=local
ARG APP_CODE=DEMO-NOTIFICATIONS
ARG SERVICE_CODE=demo-notifications-spring-boot
LABEL pe.edu.nova.git-sha="${GIT_SHA}" \
      pe.edu.nova.build-number="${BUILD_NUMBER}" \
      pe.edu.nova.app-code="${APP_CODE}" \
      pe.edu.nova.service-code="${SERVICE_CODE}" \
      org.opencontainers.image.source="https://github.com/ahincho/demo-notifications-spring-boot" \
      org.opencontainers.image.title="demo-notifications-spring-boot" \
      org.opencontainers.image.description="Nova Platform Spring Boot demo: REST endpoint backed by nova-notifications-spring-boot-starter." \
      org.opencontainers.image.licenses="UNLICENSED"

WORKDIR /app
RUN chown -R nova:nova /app

# Copy the Spring Boot fat jar (the only artifact the runtime needs).
COPY --from=builder --chown=nova:nova /build/build/libs/demo-notifications-spring-boot-1.0.0-SNAPSHOT.jar /app/app.jar

USER nova
EXPOSE 8080
STOPSIGNAL SIGTERM

# TCP-only healthcheck (the demo has no management/health endpoint).
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD nc -z localhost 8080 || exit 1

ENTRYPOINT ["/sbin/tini", "--", "java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
