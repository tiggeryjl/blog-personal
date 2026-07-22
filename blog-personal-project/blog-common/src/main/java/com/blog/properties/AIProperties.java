package com.blog.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "blog.ai")
public class AIProperties {
    private String apiKey;
    private String baseUrl;
    private ModelConfig flash = new ModelConfig();
    private ModelConfig pro = new ModelConfig();

    @Data
    public static class ModelConfig {
        private String model;
        private Double temperature;
    }
}
