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
    public static class Webhook {
        private String secretKey;
        private boolean verifySignature;
    }
} 