package pe.edu.nova.demo.notifications.spring;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.nova.java.libs.notifications.application.facade.NotificationFacade;
import pe.edu.nova.java.libs.notifications.domain.model.EmailNotification;
import pe.edu.nova.java.libs.notifications.domain.result.NotificationResult;
import pe.edu.nova.java.libs.notifications.domain.vo.EmailAddress;
import pe.edu.nova.java.libs.notifications.domain.vo.MessageBody;
import pe.edu.nova.java.libs.notifications.domain.vo.Subject;

/**
 * REST controller that exposes a single endpoint to trigger a simulated
 * email send through the Nova Notifications library. The
 * {@code NotificationFacade} is auto-wired by the Spring Boot starter.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationsController {

    private final NotificationFacade facade;

    public NotificationsController(NotificationFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/email/welcome")
    public NotificationResult sendWelcomeEmail() {
        EmailNotification email = EmailNotification.builder()
                .from(new EmailAddress("no-reply@example.com"))
                .to(new EmailAddress("customer@example.com"))
                .subject(new Subject("Welcome"))
                .body(new MessageBody("Thanks for signing up to Nova."))
                .build();
        return facade.send(email);
    }
}
