# demo-notifications-spring-boot

Example Spring Boot 4.1.0 application consuming
[`nova-java-notifications-spring-boot-starter`](../../java/nova-java-notifications-spring-boot-starter).
A single REST endpoint that triggers a simulated email send through
the library's `NotificationFacade`.

## What it demonstrates

- Auto-wiring of the `NotificationFacade` bean by the starter (the
  `nova.notifications.*` properties are read from `application.properties`).
- A real HTTP request end-to-end through the Spring Boot 4.x stack
  (Spring Web MVC, Jackson 3, embedded Tomcat).
- The Spring Boot starter's behavior under `nova.notifications.enabled=false`
  (see [`spring-boot-starter` README](../../java/nova-java-notifications-spring-boot-starter#disabling-the-library)).

## Prerequisites

- JDK 25
- The pure library and the starter must be installed in `~/.m2/repository`
  (the demo consumes them via `mavenLocal`):

  ```bash
  cd ../java/nova-java-notifications && ./mvnw install -DskipTests
  cd ../java/nova-java-notifications-spring-boot-starter && ./gradlew publishToMavenLocal
  ```

## Run

```bash
./gradlew bootRun
```

The app starts on `http://localhost:8080`. The default
`application.properties` configures the email channel with
`sendgrid` as the provider and `test-api-key-demo` as the API key
(suitable for local smoke tests; replace with a real SendGrid key for
production use).

## Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/notifications/email/welcome` | Triggers a simulated welcome email send and returns the `NotificationResult` as JSON. |

Example:

```bash
curl http://localhost:8080/api/notifications/email/welcome
# {"sent":true,"providerMessageId":"<uuid>","channel":"email",...}
```

## Configuration

`src/main/resources/application.properties`:

```properties
nova.notifications.enabled=true
nova.notifications.email.provider=sendgrid
nova.notifications.email.api-key=test-api-key-demo
nova.notifications.email.default-sender=no-reply@example.com
nova.notifications.resilience.max-attempts=1
```

Override any property at runtime via env vars or Spring Boot's
external config sources.

## Test

```bash
./gradlew test
```

The demo ships with one integration test class (`DemoApplicationTest`,
2 tests) that boots the demo on a random port with
`@SpringBootTest` and exercises the real HTTP endpoint. The
`NotificationConfiguration` bean is provided by a `@TestConfiguration`
class that bypasses the starter's auto-config binding (see the test
class's Javadoc for the rationale; the binding path is independently
verified by the starter's own integration tests, which run against a
Jackson 2.x classpath).

## Docker

The demo ships with a production-ready multi-stage `Dockerfile`
(non-root UID 1001, tini + netcat for healthchecks, OCI labels,
`-XX:MaxRAMPercentage=75.0`). Build with:

```bash
docker buildx build --build-context hostm2=$env:USERPROFILE\.m2\repository -t demo-notifications-spring-boot:1.0.0-SNAPSHOT .
docker run --rm -p 8080:8080 demo-notifications-spring-boot:1.0.0-SNAPSHOT
```

## Versioning

- `1.0.0-SNAPSHOT` — aligned with starter and library `1.0.0`.
- Java 25 toolchain.
- Spring Boot 4.1.0.

## Related

- [`nova-java-notifications`](../../java/nova-java-notifications) — pure library.
- [`nova-java-notifications-spring-boot-starter`](../../java/nova-java-notifications-spring-boot-starter) — Spring Boot auto-configuration (this demo's dependency).
- [`examples/demo-notifications-quarkus`](../demo-notifications-quarkus) — same demo on Quarkus.
- [`examples/demo-notifications-micronaut`](../demo-notifications-micronaut) — same demo on Micronaut.

---

## AI Assistance Attribution

This work was created through human-AI collaboration. The human author
(Angel Eduardo Hincho Jove, `ahincho@unsa.edu.pe`, UNSA) retains full
responsibility for the final artifact.

**AI tools used**: GitHub Copilot (Claude Opus 4.8, Sonnet 5), MiniMax
(MiniMax-M3 via paid Token Plan), OpenCode (the interactive CLI
harness used to host the session), NotebookLM, Perplexity.
Methodology: OpenSpec spec-driven development.

**Important legal note**: this artifact is **not an "AI system"** under
Article 3(1) of Regulation (EU) 2024/1689 (the EU AI Act). Article 50
transparency obligations therefore do not directly apply. This
disclosure is made voluntarily, aligned with UNESCO Principle 6
(transparency and explainability) and the R-AI requirement of the
originating challenge.

The canonical, full AI-ATTRIBUTION.md (covering the entire Nova
Platform workspace) lives at the workspace root:
[`../../AI-ATTRIBUTION.md`](../../AI-ATTRIBUTION.md).
