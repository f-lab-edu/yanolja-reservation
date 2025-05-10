package com.yanolja.areas.user.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@TestConfiguration
public class TestAuditorAwareConfig {

    @Bean
    @Primary
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("test-user"); // 임의의 사용자 ID
    }
}