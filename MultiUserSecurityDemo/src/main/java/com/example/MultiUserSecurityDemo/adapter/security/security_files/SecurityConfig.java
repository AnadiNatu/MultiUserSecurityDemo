package com.example.MultiUserSecurityDemo.adapter.security.security_files;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CompositeUserDetailService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CompositeUserDetailService userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // ================= SECURITY FILTER CHAIN =================

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Public
                        .requestMatchers("/api/auth/**", "/uploads/**").permitAll()

                        // TYPE1 (Admin)
                        .requestMatchers("/api/type1/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/type1/admin-type1/**").hasAuthority("ADMIN_TYPE1")
                        .requestMatchers("/api/type1/admin-type2/**").hasAuthority("ADMIN_TYPE2")
                        .requestMatchers("/api/type1/all-admin/**")
                        .hasAnyAuthority("ADMIN", "ADMIN_TYPE1", "ADMIN_TYPE2")

                        // TYPE2 (User)
                        .requestMatchers("/api/type2/user/**").hasAuthority("USER")
                        .requestMatchers("/api/type2/user-type1/**").hasAuthority("USER_TYPE1")
                        .requestMatchers("/api/type2/user-type2/**").hasAuthority("USER_TYPE2")
                        .requestMatchers("/api/type2/all-user/**")
                        .hasAnyAuthority("USER", "USER_TYPE1", "USER_TYPE2")

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider(userDetailsService))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // ================= AUTH PROVIDER (FIXED) =================

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService); // ✅ CORRECT CONSTRUCTOR

        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ================= PASSWORD ENCODER =================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ================= AUTH MANAGER =================

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // ================= CORS =================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(
                Arrays.asList("http://localhost:4200", "http://localhost:3000"));
        corsConfig.setAllowedMethods(
                Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(List.of("*"));
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }
}
