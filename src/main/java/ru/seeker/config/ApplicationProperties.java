package ru.seeker.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {
    private String notifyUrl;
    private double poiZipSecureMinInflateRatio;
    private long poiZipSecureMaxFileCount;
}
