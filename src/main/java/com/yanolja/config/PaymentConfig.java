package com.yanolja.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "payment")
@Getter
@Setter
public class PaymentConfig {

    private Pg pg = new Pg();
    private Webhook webhook = new Webhook();

    @Getter
    @Setter
    public static class Pg {
        private Toss toss = new Toss();
        private Kakao kakao = new Kakao();
        private Naver naver = new Naver();
    }

    @Getter
    @Setter
    public static class Toss {
        private String clientKey;
        private String secretKey;
        private String apiUrl;
        private String successUrl;
        private String failUrl;
    }

    @Getter
    @Setter
    public static class Kakao {
        private String cid;
        private String adminKey;
        private String apiUrl;
    }

    @Getter
    @Setter
    public static class Naver {
        private String clientId;
        private String clientSecret;
        private String apiUrl;
    }

    @Getter
    @Setter
    public static class Webhook {
        private String secretKey;
        private boolean verifySignature;
    }
} 