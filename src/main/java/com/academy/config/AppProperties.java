package com.academy.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {
    private String url;
    private String frontendUrl;
    private String name;
}
