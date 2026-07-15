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

## What was built for this technical challenge

This repository is the **Spring Boot 4.1.0 example app** (Nivel 3) of
the Nova Platform notifications module. It is the smallest end-to-end
application that proves the Spring Boot Nivel 2 starter works in a
real Spring Boot project: a single REST endpoint that triggers a
simulated email send through the library's `NotificationFacade` and
returns the `NotificationResult` as JSON.

### Role in the Nova Platform

- **Nivel**: 3 (application / example).
- **Depends on**: the Spring Boot Nivel 2 starter
  (`pe.edu.nova.java.starters:nova-notifications-spring-boot-starter:1.0.0`
  in the sibling `java/nova-java-notifications-spring-boot-starter`
  repo), which transitively pulls in the Nivel 1 pure library.
- **Pattern**: archetype / reference app. The demo is small on
  purpose so the entire flow (HTTP → controller → facade → provider →
  result) fits in one read-through.

### What this repository delivers

- A **single REST endpoint** at `/api/notifications/email/welcome`
  annotated with `@RestController` that triggers a simulated email
  send and returns the `NotificationResult` as JSON.
- **Real HTTP round-trip** through the Spring Boot 4.x stack
  (Spring MVC, Jackson 3, embedded Tomcat).
- A **JUnit 5 integration test** (`DemoApplicationTest`) that boots
  the demo on a random port with `@SpringBootTest` and exercises
  the endpoint end-to-end. The `NotificationConfiguration` bean is
  provided by a `@TestConfiguration` class that bypasses the
  starter's auto-config binding (see the test class's Javadoc for
  the rationale).
- A **production-ready multi-stage Dockerfile** (non-root UID 1001,
  tini + netcat for healthchecks, OCI labels,
  `-XX:MaxRAMPercentage=75.0`).

### Quality gates verified

- **Gradle build + Checkstyle + JUnit 5 integration test = green**
  in the CI/CD pipeline (`ahincho/demo-notifications-spring-boot`
  GitHub Actions).
- **Local run verified** with `./gradlew bootRun`, the endpoint
  returning the expected JSON payload.

### How to reproduce

```bash
./gradlew clean build
./gradlew bootRun
```

Then exercise the endpoint:

```bash
curl http://localhost:8080/api/notifications/email/welcome
# {"sent":true,"providerMessageId":"<uuid>","channel":"email",...}
```

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

This repository was produced through human-AI collaboration. The human
author (Angel Eduardo Hincho Jove, `ahincho@unsa.edu.pe`, Universidad
Nacional de San Agustín de Arequipa — UNSA) retains full responsibility
for the final artifact and for every commit accepted into the repository.

### Challenge context

This work was produced in response to the technical challenge
described in `Reto-Tecnico-Backend.pdf`. Section 2.5 of the challenge
mandates an explicit AI disclosure in the README when AI is used. This
section fulfils that requirement (R-AI / **R**esponsible **AI**
disclosure) and is also aligned with:

- **Regulation (EU) 2024/1689** ("EU AI Act"), Article 3(1)
  (definition of "AI system") and Article 50 (transparency obligations
  for deployers of certain AI systems).
- **UNESCO Recommendation on the Ethics of Artificial Intelligence**
  (2021), adopted by 193 Member States, **Principle 6: Transparency and
  explainability**.

### AI tools used in this repository

| Tool | Provider | Model / Role | Access tier |
|---|---|---|---|
| GitHub Copilot | GitHub / Anthropic | Claude Opus 4.8, Claude Sonnet 5 (in-editor suggestions) | Licensed |
| MiniMax Token Plan | MiniMax | MiniMax-M3 (the model used for long-form generation and refactoring in the OpenCode session) | Paid (personal) |
| OpenCode | anomalyco (`opencode.ai`) | Interactive CLI harness — **not a model**, only the session/UI | Free (CLI) |
| OpenSpec | Fission AI | Spec-driven development framework (used for the meta-framework backlog) | Licensed |
| NotebookLM | Google | Gemini (cross-document synthesis of the challenge PDF and ADRs) | Free |
| Perplexity | Perplexity AI | Sonar / Pro Search (lookup of latest framework versions and release dates) | Free |

> *Important distinction*: OpenCode is the interactive CLI harness in
> which the AI-assisted development session took place (with MiniMax-M3
> as the underlying model). OpenCode is **not a model** and **not a
> license/subscription manager** — the subscription providing access to
> the model is the **MiniMax Token Plan** listed above. The two rows
> are kept deliberately separate so that anyone reading the disclosure
> can identify exactly which entity provides the model and which entity
> provides the session/UI.

### Scope of AI assistance in this repository

- Drafting the **initial code skeletons** (sealed interfaces, value
  objects, port interfaces, provider stubs).
- Drafting **unit tests** for value objects, the error hierarchy, the
  template resolver, the rate-limiter, the circuit-breaker state
  machine, and the i18n message bundle (Spanish / English).
- **Documentation drafts** of this README and the inline Javadoc.
- **Build infrastructure** snippets for the reusable CI/CD workflows
  in `ahincho/nova-devops`.
- **Cross-checking** the published provider documentation (SendGrid,
  Mailgun, Twilio, Firebase) for the authentication-header / payload
  shape used in the per-provider adapters (no live API calls are made).

### Human contributions (author: Angel Eduardo Hincho Jove)

The following decisions and artifacts are **authored and approved by
the human**, not delegated to AI:

- **Architecture**: hexagonal / ports-and-adapters layout, framework
  isolation in the core library, the five-level meta-framework
  (Nivel 1 = pure library, Nivel 2 = starter/extension,
  Nivel 3 = application) per `ADR-001` and `ADR-015`.
- **Scope**: which channels and providers are in scope for the
  challenge (Email / SMS / Push mandatory, Slack optional) and which
  features are deferred.
- **Version pinning**: Java 25, Spring Boot 4.1.0, Quarkus 3.33.2.1
  LTS, Micronaut 5.0.4, Gradle 9.5.1, Maven 3.9.x. Each pin was
  cross-checked against the latest stable release and the framework
  vendor's LTS roadmap.
- **Quality gates**: 80 % JaCoCo coverage, Checkstyle Nova style,
  ArchUnit test enforcing zero framework leakage in the core library.
- **Build infrastructure**: Maven for the core (T-02 of the challenge
  mandates Maven), Gradle 9.5.1 for the framework starters and demos
  (consistency with the rest of the Nova Platform).
- **Final review and approval** of every commit, including a final
  end-to-end run of `./mvnw verify` and `./gradlew build` against
  JDK 25 before tagging the release.
- **Legal classification**: the determination that the artifacts
  shipped here (a deterministic Java library + framework adapters +
  example apps) are **not "AI systems"** under EU AI Act Article 3(1)
  and therefore do not directly attract Article 50 obligations (see
  the legal clarification below).

### Methodology

The work followed a **Spec-Driven Development** approach using
OpenSpec:

1. Requirements were captured as structured specifications before any
   code was written (`CHALLENGE.md`, `REQUIREMENTS.md`, the ADRs).
2. AI assistance operated against those specifications, not in the
   abstract.
3. The human author reviewed and approved each artifact (build, test,
   commit) before it was accepted into the repository.

### Legal clarification (EU AI Act)

A deterministic Java notifications library does not "infer" outputs,
does not generate predictions / recommendations / decisions, and does
not exhibit autonomy or adaptiveness after deployment. Therefore the
artifacts shipped in this repository are **not "AI systems"** within
the meaning of Article 3(1) of Regulation (EU) 2024/1689 (the EU AI
Act), and Article 50 does not directly impose obligations on them.

This disclosure is nevertheless made:

- **By contractual / academic requirement**: per the R-AI requirement
  of the originating technical challenge (challenge PDF §2.5).
- **Voluntarily**, in alignment with the spirit of the EU AI Act
  transparency principles and UNESCO Principle 6.
- **In the interest of authorship transparency** for the open-source
  community.

### Canonical disclosure

The full Nova Platform AI attribution (covering every repository in
the workspace — pure libraries, framework adapters, demos, tooling
and documentation) lives in a single canonical file at the workspace
root:

[`../../AI-ATTRIBUTION.md`](../../AI-ATTRIBUTION.md)

This per-repository section is a compact summary that points back to
that canonical file as the source of truth for the full disclosure;
the legal analysis and the human-contributions audit are not
duplicated in every repository on purpose.

### Change log

- **2026-07-15** — Initial disclosure created.