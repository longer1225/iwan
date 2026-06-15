package com.iwan.blog.config;

import com.iwan.blog.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/user/register", "/api/v1/user/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/user/info").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/v1/user/info").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/upload/avatar").authenticated()
                .requestMatchers("/api/v1/articles", "/api/v1/articles/**").permitAll()
                .requestMatchers("/api/v1/comments", "/api/v1/comments/**").permitAll()
                .requestMatchers("/api/v1/tags", "/api/v1/tags/**").permitAll()
                .requestMatchers("/api/v1/search", "/api/v1/search/**").permitAll()
                .requestMatchers("/api/v1/categories", "/api/v1/categories/**").permitAll()
                .requestMatchers("/api/v1/friends", "/api/v1/friends/**").authenticated()
                .requestMatchers("/api/v1/action/**").authenticated()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
