package ru.seeker.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {
    private double poiZipSecureMinInflateRatio;
    private long poiZipSecureMaxFileCount;
    private int minFilledCellRowSave;
    private String adminIp;
}
