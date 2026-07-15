plugins {
    java
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.6"
}

repositories {
    mavenLocal()
    mavenCentral()
    // GitHub Packages of the Spring Boot starter (peers cross-repo dependency).
    // The starter transitively depends on pe.edu.nova.java.libs:nova-notifications:1.0.0
    // which is also published to its own repo (nova-java-notifications); Gradle will
    // resolve that automatically as long as both maven {} entries are configured.
    // NOVA_PACKAGES_READ_TOKEN is a PAT with packages:read scope; falls back to
    // GITHUB_TOKEN if not set (GITHUB_TOKEN can read packages within the same repo
    // but not across repos, so cross-repo dependencies would fail without the PAT).
    maven {
        name = "GitHubPackages-NovaNotifications"
        url = uri("https://maven.pkg.github.com/ahincho/nova-java-notifications-spring-boot-starter")
        val token = System.getenv("NOVA_PACKAGES_READ_TOKEN")
            ?: System.getenv("NOVA_RELEASE_PAT")
            ?: System.getenv("GITHUB_TOKEN")
        if (!token.isNullOrBlank()) {
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: "x-access-token"
                password = token
            }
        }
    }
    maven {
        name = "GitHubPackages-NovaNotifications-Core"
        url = uri("https://maven.pkg.github.com/ahincho/nova-java-notifications")
        val token = System.getenv("NOVA_PACKAGES_READ_TOKEN")
            ?: System.getenv("NOVA_RELEASE_PAT")
            ?: System.getenv("GITHUB_TOKEN")
        if (!token.isNullOrBlank()) {
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: "x-access-token"
                password = token
            }
        }
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // The Nova starter (locally published) that wires the notifications library
    // into Spring Boot via auto-configuration.
    implementation("pe.edu.nova.java.starters:nova-notifications-spring-boot-starter:1.0.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

group = "pe.edu.nova"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

checkstyle {
    toolVersion = "10.20.1"
    configFile = file("config/checkstyle/checkstyle.xml")
}