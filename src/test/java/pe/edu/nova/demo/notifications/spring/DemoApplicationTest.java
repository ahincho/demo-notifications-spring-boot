package pe.edu.nova.demo.notifications.spring;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import pe.edu.nova.java.libs.notifications.application.facade.NotificationFacade;
import pe.edu.nova.java.libs.notifications.domain.result.NotificationResult;
import pe.edu.nova.java.libs.notifications.domain.vo.EmailAddress;
import pe.edu.nova.java.libs.notifications.infrastructure.configuration.EmailConfiguration;
import pe.edu.nova.java.libs.notifications.infrastructure.configuration.EmailProvider;
import pe.edu.nova.java.libs.notifications.infrastructure.configuration.NotificationConfiguration;
import pe.edu.nova.java.libs.notifications.infrastructure.configuration.ResilienceConfiguration;

/**
 * Integration test for the Spring Boot demo. Boots the full demo
 * application on a random port via {@code @SpringBootTest}, then issues a
 * real HTTP request through Spring's {@link RestClient} against the
 * {@code /api/notifications/email/welcome} endpoint exposed by
 * {@link NotificationsController}.
 *
 * <p>Spring Boot 4.x dropped both {@code MockMvc} and {@code TestRestTemplate}
 * from {@code spring-boot-test}. The recommended replacement for a
 * blocking servlet demo is a real server with {@code RestClient} (or
 * {@code WebTestClient} for reactive apps). We intentionally avoid
 * additional test libraries (no RestAssured, no MockMvc) so the demo's test
 * deps stay as small as possible.
 *
 * <p>The {@link DemoTestConfig} inner class manually creates the
 * {@code NotificationConfiguration} bean for the test context. This
 * bypasses the {@code @EnableConfigurationProperties} binder in
 * {@code NotificationsAutoConfiguration} because Spring Boot 4.x's
 * configuration-properties binder has a known interaction with the
 * Jackson version on the classpath (the binder's behavior for nested
 * static {@code @ConfigurationProperties} classes depends on which
 * Jackson is present). The auto-config's own binding path is
 * independently verified by
 * {@code SpringBootSmokeTest} in the starter module — that test runs
 * against a Jackson-2-only classpath and passes. The test here focuses
 * on what the DEMO does: wire the {@code NotificationFacade} into the
 * controller and serve a real HTTP request end-to-end.
 */
@SpringBootTest(classes = DemoApplicationTest.DemoTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoApplicationTest {

    @LocalServerPort
    private int port;

    private final RestClient restClient = RestClient.create();

    @Autowired
    private NotificationFacade facade;

    @Test
    void starterAutoWiresNotificationFacadeIntoTheDemo() {
        assertThat(facade).isNotNull();
    }

    @Test
    void getWelcomeEmailEndpointReturns200AndSentResult() {
        String url = "http://localhost:" + port + "/api/notifications/email/welcome";

        NotificationResult result = restClient.get()
                .uri(url)
                .retrieve()
                .body(NotificationResult.class);

        assertThat(result).isNotNull();
        assertThat(result.isSent()).isTrue();
        assertThat(result.providerMessageId()).isPresent();
    }

    @TestConfiguration
    static class DemoTestConfig {
        @Bean
        NotificationConfiguration notificationConfiguration() {
            return NotificationConfiguration.builder()
                    .email(EmailConfiguration.builder()
                            .provider(EmailProvider.SENDGRID)
                            .apiKey("test-api-key-demo")
                            .defaultSender(new EmailAddress("no-reply@example.com"))
                            .build())
                    .resilience(ResilienceConfiguration.disabled())
                    .build();
        }
    }
}