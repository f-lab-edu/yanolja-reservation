package com.yanolja.common.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    private String secret;
    private long accessTokenExpiration;  // 15분
    private long refreshTokenExpiration; // 2주
    private String header;
    private String prefix;

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setAccessTokenExpiration(long accessTokenExpiration) {
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public void setRefreshTokenExpiration(long refreshTokenExpiration) {
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
} 