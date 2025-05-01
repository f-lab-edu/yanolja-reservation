package com.yanolja.areas.user.service;

import com.yanolja.areas.user.domain.SocialProvider;
import com.yanolja.areas.user.domain.User;
import com.yanolja.areas.user.domain.UserDetail;
import com.yanolja.areas.user.repository.UserRepository;
import com.yanolja.common.exception.ErrorCode;
import com.yanolja.common.exception.UserException;
import com.yanolja.common.oauth2.OAuth2UserInfo;
import com.yanolja.common.oauth2.OAuth2UserInfoFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("OAuth2 인증 처리 중 오류 발생", ex);
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        SocialProvider socialProvider = getSocialProvider(registrationId);
        
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                socialProvider, oAuth2User.getAttributes());

        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new UserException(ErrorCode.MISSING_EMAIL);
        }

        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            
            // 이미 소셜 로그인으로 가입했는지 확인
            if (user.getSocialProvider() != null && 
                    !user.getSocialProvider().equals(socialProvider)) {
                throw new UserException(ErrorCode.ACCOUNT_ALREADY_LINKED, 
                        "이미 다른 소셜 계정(" + user.getSocialProvider().getLabel() + ")으로 가입된 이메일입니다.");
            }
            
            // 기존 사용자의 소셜 정보 업데이트
            user.updateSocialInfo(socialProvider, oAuth2UserInfo.getId(), oAuth2UserInfo.getImageUrl());
            user = userRepository.save(user);
        } else {
            // 신규 사용자 생성
            user = User.createSocialUser(
                    oAuth2UserInfo.getName(),
                    oAuth2UserInfo.getEmail(),
                    socialProvider,
                    oAuth2UserInfo.getId(),
                    oAuth2UserInfo.getImageUrl()
            );
            user = userRepository.save(user);
            log.info("소셜 로그인 회원가입 완료: {} ({})", user.getEmail(), socialProvider);
        }

        return UserDetail.create(user, oAuth2User.getAttributes());
    }

    private SocialProvider getSocialProvider(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> SocialProvider.GOOGLE;
            case "kakao" -> SocialProvider.KAKAO;
            default -> throw new UserException(ErrorCode.INVALID_PROVIDER);
        };
    }
} 