package com.pyokemon.gateway_service.config;

import com.pyokemon.gateway_service.security.exception.RestAccessDeniedHandler;
import com.pyokemon.gateway_service.security.exception.RestAuthenticationEntryPoint;
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

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final JwtTokenValidator jwtTokenValidator;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain applicationSecurity(HttpSecurity http) throws Exception {
        http
                // cors 설정은 WebConfig의 corsFilter를 사용
                .cors(cors -> {})
                .csrf(AbstractHttpConfigurer::disable)
                .securityMatcher("/**")
                .sessionManagement(sessionManagementConfigurer
                        -> sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // SSL 클라이언트 인증 비활성화
                .x509(AbstractHttpConfigurer::disable)
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenValidator),
                        UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling((exceptionConfig) ->
                        exceptionConfig
                                .authenticationEntryPoint(authenticationEntryPoint)
                                .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(registry -> registry
                        // 임시로 모든 API에 대한 인증 해제
                        .anyRequest().permitAll()
                        // .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // .requestMatchers("/api/gateway/v1/**").permitAll()

                        // .requestMatchers(
                        //         "/account/login",
                        //         "/account/app/login",
                        //         "/account/users"
                        // ).permitAll()
                        // // Todo: 경로 맞추기
                        // .requestMatchers(
                        //         "/api/events",
                        //         "/api/events/open-today",
                        //         "/api/events/to-be-opened"
                        // ).permitAll()

                        // .requestMatchers("/api/events/**").permitAll()
                        // .requestMatchers("/api/event-schedules/**").permitAll()
                        // .requestMatchers("/api/seats").permitAll()

                        // .requestMatchers(
                        //         "/health/**",
                        //         "/actuator/**",
                        //         "/actuator/health/**"
                        // ).permitAll()

                        // .anyRequest().authenticated()
                );
        return http.build();
    }

    // corsConfigurationSource 빈 제거
}