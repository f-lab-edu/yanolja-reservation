package com.yanolja.common.oauth2;

import com.yanolja.areas.user.domain.SocialProvider;
import com.yanolja.common.exception.ErrorCode;
import com.yanolja.common.exception.UserException;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(SocialProvider socialProvider, Map<String, Object> attributes) {
        return switch (socialProvider) {
            case GOOGLE -> new GoogleOAuth2UserInfo(attributes);
            case KAKAO -> new KakaoOAuth2UserInfo(attributes);
            default -> throw new UserException(ErrorCode.INVALID_PROVIDER);
        };
    }
} 