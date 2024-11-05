package com.stlghana.admin_service.config;


import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("voip")
@Data
public class VOIPProperties {

    @NestedConfigurationProperty
    private final @Valid Keycloak keycloak = new Keycloak();

    @Data
    public static class Keycloak {
        private String authServerUrl;
        private String realm;
        private String clientId;
        private String clientSecret;
        private String tokenEndpoint;
    }
}
