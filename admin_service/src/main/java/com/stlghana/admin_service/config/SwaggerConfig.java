package com.stlghana.admin_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Objects;


@Configuration
public class SwaggerConfig {

    /**
     * Mapping the variable KEYCLOAK_TOKEN_PATH to the token URL in the properties file.
     * This URL is used for token generation.
     */
    @Value("${springdoc.swagger-ui.oauth.token-url}")
    private String KEYCLOAK_TOKEN_PATH;

    @Value("${springdoc.swagger-ui.oauth.authorization-grant-type}")
    private String KEYCLOAK_GRANT_TYPE;

    /**
     * This method creates a custom OpenAPI configuration.
     * It sets up security schemes for OAuth2, specifically using the client credentials flow.
     *
     * @return An instance of OpenAPI with the configured security schemes.
     */
    @Bean
    public OpenAPI customOpenAPI(){

        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("spring_oauth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.OAUTH2)
                                        .description("Oauth2 flow")
                                        .flows(
                                                Objects.nonNull(KEYCLOAK_GRANT_TYPE) && KEYCLOAK_GRANT_TYPE.replace("-", "_")
                                                        .equalsIgnoreCase("client_credentials")
                                                ? new OAuthFlows()
                                                    .clientCredentials(new OAuthFlow().tokenUrl(KEYCLOAK_TOKEN_PATH))
                                                : new OAuthFlows()
                                                    .password(new OAuthFlow().tokenUrl(KEYCLOAK_TOKEN_PATH))
                                        )
                        )
                )
                .security(Collections.singletonList(
                        new SecurityRequirement().addList("spring_oauth")
                ));
    }
}
