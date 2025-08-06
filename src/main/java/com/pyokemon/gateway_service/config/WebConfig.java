package com.pyokemon.gateway_service.config;

import com.pyokemon.gateway_service.common.filter.RequestResponseLoggingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // CORS 설정은 WebSecurityConfig에서 완전히 처리
    
    @Bean
    public FilterRegistrationBean<RequestResponseLoggingFilter> loggingFilter() {
        FilterRegistrationBean<RequestResponseLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestResponseLoggingFilter());
        
        // 중복 실행 방지를 위해 더 구체적인 URL 패턴 설정 
        // "/*" 대신 "/account/*", "/event/*" 등 구체적인 패턴 사용
        registrationBean.addUrlPatterns("/account/*", "/event/*", "/payment/*", "/did/*", "/noti/*", "/bff/*");
        
        registrationBean.setOrder(Ordered.LOWEST_PRECEDENCE);
        return registrationBean;
    }
}