package com.pyokemon.gateway_service.config;


import com.pyokemon.gateway_service.security.filter.JwtAuthenticationFilter;
import com.pyokemon.gateway_service.security.jwt.JwtTokenValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final JwtTokenValidator jwtTokenValidator;


    @Bean
    public SecurityFilterChain applicationSecurity(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .securityMatcher("/**")
                .sessionManagement(sessionManagementConfigurer
                        -> sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .x509(AbstractHttpConfigurer::disable)
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenValidator), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(registry -> registry
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Account Api 허용
                        .requestMatchers(AccountApi.PERMIT_ALL).permitAll()
                        // Event Api 허용
                        .requestMatchers(EventApi.PERMIT_ALL).permitAll()
                        // System Api 허용
                        .requestMatchers(SystemApi.PERMIT_ALL).permitAll()
                        // Gateway Api 허용
                        .requestMatchers(GatewayApi.PERMIT_ALL).permitAll()
                        // 나머지 요청은 인증 필요
                        .anyRequest().authenticated()
                );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource
    corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.addAllowedOrigin("http://localhost:5173");
        configuration.addAllowedOrigin("http://localhost:6080");
        configuration.addAllowedOrigin("https://pyokemon.synology.me");
        configuration.addAllowedOrigin("http://pyokemon.synology.me");
        configuration.addAllowedHeader("*");
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // 계정 관련 허용 엔드포인트
    public static class AccountApi {
        public static final String[] PERMIT_ALL = {
                "/account/api/login",
                "/account/api/app/login",
                "/account/api/users"
        };
    }

    // 이벤트 관련 허용 엔드포인트
    public static class EventApi {
        public static final String[] PERMIT_ALL = {
                "/event/api/events",
                "/event/api/events/open-today",
                "/event/api/events/to-be-opened",
                "/event/api/events/**",
                "/event/api/event-schedules/**",
                "/event/api/seats"
        };
    }

    // 시스템 헬스체크 관련 허용 엔드포인트
    public static class SystemApi {
        public static final String[] PERMIT_ALL = {
                "/health/**",
                "/actuator/**",
                "/actuator/health/**"
        };
    }

    // 게이트웨이 관련 허용 엔드포인트
    public static class GatewayApi {
        public static final String[] PERMIT_ALL = {
                "/api/gateway/v1/**"
        };
    }
}