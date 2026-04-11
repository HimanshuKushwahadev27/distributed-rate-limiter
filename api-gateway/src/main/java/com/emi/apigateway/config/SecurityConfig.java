package com.emi.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
	private final String [] WHITELIST= {
			"/v3/api-docs/**",
			"/swagger-ui.html",
			"/swagger-ui/**",
			"/actuator/**",
			"/swagger-resources/**",
			"/api-docs/**",
			"/aggregate/**",
	};
	
    @Bean
    SecurityWebFilterChain  securityWebFilterChain(ServerHttpSecurity  http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                    .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll() 
                	.pathMatchers(WHITELIST).permitAll()
                    .anyExchange().authenticated()
                )
                .cors((Customizer.withDefaults()))
                .oauth2ResourceServer(oauth2 ->
                    oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(new ReactiveJwtAuthConverter()))
                )
                .build();
    }
}  