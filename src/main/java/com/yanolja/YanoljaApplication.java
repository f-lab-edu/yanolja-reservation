package com.yanolja;

import com.yanolja.common.auditing.AuditorAwareImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorProvider") //JPA 기본필드 입력값 자동화 처리(등록일 등)
@EnableScheduling // 스케줄링 활성화
public class YanoljaApplication {
    public static void main(String[] args) {
        SpringApplication.run(YanoljaApplication.class, args);
    }

    @Bean
    @ConditionalOnMissingBean(name = "auditorProvider")
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }
} 