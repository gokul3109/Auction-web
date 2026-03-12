package com.auctionweb.auction;

import com.auctionweb.auction.security.JwtAuthFilter;
import com.auctionweb.auction.service.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtUtil jwtUtil;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // Spring Security 6: must set requireExplicitSave(false) so that
            // SecurityContextHolder.setContext() is respected by the authorization layer
            .securityContext(sc -> sc.requireExplicitSave(false))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/", "/api/health").permitAll()
                // Anyone can browse auctions (GET), but creating/editing/deleting requires auth
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/auctions", "/api/auctions/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Allowed origins — add production URL here when deploying
        config.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "http://localhost:3001"
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        config.setAllowedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "Accept",
            "X-Requested-With"
        ));

        // Expose Authorization header so frontend can read JWT from response if needed
        config.setExposedHeaders(List.of("Authorization"));

        // Allow cookies/credentials (needed for SSE too)
        config.setAllowCredentials(true);

        // Cache preflight response for 1 hour
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
