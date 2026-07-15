package pe.edu.nova.demo.notifications.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demo entry point. The {@code nova-notifications-spring-boot-starter} is on
 * the classpath, so {@code NotificationFacade} is auto-wired and ready to
 * inject anywhere in the app.
 */
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
