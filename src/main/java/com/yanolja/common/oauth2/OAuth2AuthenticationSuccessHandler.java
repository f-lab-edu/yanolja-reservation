package com.yanolja.common.oauth2;

import com.yanolja.common.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

import static com.yanolja.common.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    
    @Value("${spring.profiles.active:local}")
    private String activeProfile;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.debug("Authentication success: {}", authentication);
        String targetUrl = determineTargetUrl(request, response, authentication);
        log.debug("Target URL: {}", targetUrl);

        if (response.isCommitted()) {
            log.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Optional<String> redirectUri = getCookieValue(request, REDIRECT_URI_PARAM_COOKIE_NAME);
        
        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());
        
        String accessToken = tokenProvider.createAccessToken(authentication);
        String refreshToken = tokenProvider.createRefreshToken(authentication);
        
        // 로컬 개발 환경에서는 secure 플래그 비활성화
        boolean isSecure = !"local".equals(activeProfile) && !"dev".equals(activeProfile);
        
        Cookie accessTokenCookie = new Cookie("access_token", accessToken);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setHttpOnly(false); // JavaScript에서 접근 가능하도록 변경
        accessTokenCookie.setSecure(isSecure);
        response.addCookie(accessTokenCookie);
        
        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(false); // JavaScript에서 접근 가능하도록 변경
        refreshTokenCookie.setSecure(isSecure);
        response.addCookie(refreshTokenCookie);
        
        log.debug("OAuth2 토큰 쿠키 설정 완료 - Secure: {}", isSecure);
        
        return targetUrl;
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        // Remove the cookie directly instead of using the repository
        Cookie cookie = new Cookie(REDIRECT_URI_PARAM_COOKIE_NAME, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private Optional<String> getCookieValue(HttpServletRequest request, String name) {
        return Optional.ofNullable(request.getCookies())
                .flatMap(cookies -> {
                    for (Cookie cookie : cookies) {
                        if (cookie.getName().equals(name)) {
                            return Optional.of(cookie.getValue());
                        }
                    }
                    return Optional.empty();
                });
    }
} 