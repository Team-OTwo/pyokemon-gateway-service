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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

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
                // CORS 설정을 완전히 비활성화하는 대신, CorsConfigurationSource를 통해 동일한 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
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
                        //.anyRequest().permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers("/api/gateway/v1/**").permitAll()

                        .requestMatchers(
                                "/account/api/login",
                                "/account/api/app/login",
                                "/account/api/users"
                        ).permitAll()
                        // Todo: 경로 맞추기
                        .requestMatchers(
                                "/event/api/events",
                                "/event/api/events/open-today",
                                "/event/api/events/to-be-opened"
                        ).permitAll()

                        .requestMatchers("/event/api/events/**").permitAll()
                        .requestMatchers("/event/api/event-schedules/**").permitAll()
                        .requestMatchers("/event/api/seats").permitAll()

                        .requestMatchers(
                                "/health/**",
                                "/actuator/**",
                                "/actuator/health/**"
                        ).permitAll()

                        .anyRequest().authenticated()
                );
        return http.build();
    }

    // Spring Security에서 사용할 CorsConfigurationSource를 직접 정의
    // WebConfig와 동일한 설정을 사용하여 중복되는 헤더가 생성되지 않게 함
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(false); // 크리덴셜이 필요하면 true로 변경하고 아래 오리진 수정
        configuration.addAllowedOrigin("*"); // 크리덴셜 true일 경우 특정 오리진(예: "http://localhost:5173")으로 변경
        configuration.addAllowedHeader("*");
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}