package ru.seeker;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import ru.seeker.config.ApplicationProperties;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

@Slf4j
@EnableAsync
@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class EventsApp {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(EventsApp.class);
        app.setDefaultProperties(Map.of("spring.profiles.active", "dev"));
        logApplicationStartup(app.run(args).getEnvironment());
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"));
    }

    private static void logApplicationStartup(Environment env) {
        String protocol = Optional.ofNullable(env.getProperty("server.ssl.key-store")).map(key -> "https").orElse("http");
        String serverPort = env.getProperty("server.port");
        String contextPath = Optional
                .ofNullable(env.getProperty("server.servlet.context-path"))
                .filter(StringUtils::isNotBlank)
                .orElse("/");
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("The host name could not be determined, using `localhost` as fallback");
        }
        log.info(
                """
                            ----------------------------------------------------------
                            \tApplication '{}' is running! Access URLs:"
                            \tLocal: \t\t{}://localhost:{}{}"
                            \tExternal: \t{}://{}:{}{}"
                            \tSwagger: \t{}://localhost:{}{}{}
                            \tProfile(s):\t{}
                            ----------------------------------------------------------
                        """,
                env.getProperty("spring.application.name"),
                protocol, serverPort, contextPath,
                protocol, hostAddress, serverPort, contextPath,
                protocol, serverPort, contextPath, "/swagger-ui/index.html",
                env.getActiveProfiles().length == 0 ? env.getDefaultProfiles() : env.getActiveProfiles());
    }
}
