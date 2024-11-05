package com.stlghana.admin_service.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public static final String CONTEXT_PATH = "/api/v1/admin_service";

    private static final String[] SWAGGER_ENDPOINTS = {

            "/", HttpMethod.GET.name(),
            "/actuator/**",
            CONTEXT_PATH + "/swagger-ui/**",
            CONTEXT_PATH + "/configuration/**",
            CONTEXT_PATH + "/swagger-resources/**",
            CONTEXT_PATH + "/swagger-ui.html/**",
            CONTEXT_PATH + "/api-docs/**",
            CONTEXT_PATH + "/webjars/**",
            CONTEXT_PATH +  "/assets/**",
            CONTEXT_PATH +  "/static/**"
    };


    /**
     * This method configures the security filter chain for the HTTP security.
     * It uses keycloak for securing rest api calls
     *
     * @param http The HTTP security configuration.
     * @return The configured security filter chain.
     * @throws Exception If an error occurs while configuring the security filter chain.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(SWAGGER_ENDPOINTS)
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()))
                .build();
    }


    /**
     * This method is the Cors Configuration that allow Application CRUD on the server
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedMethods(List.of("HEAD",
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.applyPermitDefaultValues();
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    /**
     * Configures the JWT Authentication Converter to extract roles from the JWT token.
     *
     * This method defines a custom `JwtAuthenticationConverter` that is used to extract
     * authorities (roles) from the JWT token, specifically from the "realm_access" claim
     * which is commonly found in tokens issued by Keycloak or similar identity providers.
     *
     * The `realm_access` claim contains the roles assigned to the user at the realm level,
     * and these roles are converted into Spring Security's `GrantedAuthority` objects to be
     * used for role-based access control within the application.
     *
     * @return JwtAuthenticationConverter The custom JWT authentication converter.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthorityConverter = jwt -> {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            List<String> clientRoles = new ArrayList<>();
            if(realmAccess != null) {
                Object realm_roles = realmAccess.get("roles");
                if(realm_roles != null){
                    clientRoles.addAll(((List<Object>) realm_roles)
                            .stream().map(Object::toString)
                            .toList());
                }
            }
            return clientRoles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        };
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthorityConverter);
        return jwtAuthenticationConverter;
    }



}
