package com.pyokemon.gateway_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;

@Configuration
public class TracingConfig {

    /**
     * AOP를 통한 Observation 지원 활성화를 위한 빈 등록
     * @Observed 애노테이션을 사용한 메서드에 자동으로 trace 정보가 생성됨
     */
    @Bean
    ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        return new ObservedAspect(observationRegistry);
    }
} 