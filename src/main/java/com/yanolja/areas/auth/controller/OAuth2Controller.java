package com.yanolja.areas.auth.controller;

import com.yanolja.areas.auth.domain.SocialProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/auth/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    @Operation(summary = "소셜 로그인", description = "OAuth2 소셜 로그인 요청을 처리합니다.")
    @GetMapping("/login")
    public RedirectView login(@RequestParam("provider") String provider, 
                              @RequestParam(value = "redirect_uri", required = false) String redirectUri) {
        
        String baseUri = "/oauth2/authorize/";
        String targetUri = baseUri + provider.toLowerCase();
        
        if (redirectUri != null && !redirectUri.isEmpty()) {
            targetUri += "?redirect_uri=" + redirectUri;
        }
        
        return new RedirectView(targetUri);
    }
    
    @Operation(summary = "소셜 로그인 제공자 목록", description = "지원하는 소셜 로그인 제공자 목록을 반환합니다.")
    @GetMapping("/providers")
    public SocialProvider[] getProviders() {
        return SocialProvider.values();
    }
} 