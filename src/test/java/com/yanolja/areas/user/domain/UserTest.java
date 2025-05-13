package com.yanolja.areas.user.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    @DisplayName("일반 사용자 생성 테스트")
    void createUser() {
        // given
        String name = "테스트유저";
        String email = "test@example.com";
        String encodedPassword = "encodedPassword";
        String phone = "01012345678";

        // when
        User user = User.createUser(name, email, encodedPassword, phone);

        // then
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPassword()).isEqualTo(encodedPassword);
        assertThat(user.getPhone()).isEqualTo(phone);
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
        assertThat(user.getWithdrawalYn()).isEqualTo("N");
    }

    @Test
    @DisplayName("소셜 로그인 사용자 생성 테스트")
    void createSocialUser() {
        // given
        String name = "소셜유저";
        String email = "social@example.com";
        SocialProvider socialProvider = SocialProvider.GOOGLE;
        String socialId = "social123";
        String profileImageUrl = "https://example.com/profile.jpg";

        // when
        User user = User.createSocialUser(name, email, socialProvider, socialId, profileImageUrl);

        // then
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getSocialProvider()).isEqualTo(socialProvider);
        assertThat(user.getSocialId()).isEqualTo(socialId);
        assertThat(user.getProfileImageUrl()).isEqualTo(profileImageUrl);
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
        assertThat(user.getWithdrawalYn()).isEqualTo("N");
        assertThat(user.getPassword()).isNull();
    }

    @Test
    @DisplayName("관리자 사용자 생성 테스트")
    void createAdmin() {
        // given
        String name = "관리자";
        String email = "admin@example.com";
        String encodedPassword = "encodedPassword";
        String phone = "01098765432";

        // when
        User user = User.createAdmin(name, email, encodedPassword, phone);

        // then
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPassword()).isEqualTo(encodedPassword);
        assertThat(user.getPhone()).isEqualTo(phone);
        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(user.getWithdrawalYn()).isEqualTo("N");
    }

    @Test
    @DisplayName("비밀번호 변경 테스트")
    void changePassword() {
        // given
        User user = User.createUser("테스트유저", "test@example.com", "oldPassword", "01012345678");
        String newPassword = "newPassword";

        // when
        user.changePassword(newPassword);

        // then
        assertThat(user.getPassword()).isEqualTo(newPassword);
    }

    @Test
    @DisplayName("소셜 로그인 정보 업데이트 테스트")
    void updateSocialInfo() {
        // given
        User user = User.createUser("테스트유저", "test@example.com", "password", "01012345678");
        SocialProvider socialProvider = SocialProvider.KAKAO;
        String socialId = "kakao123";
        String profileImageUrl = "https://example.com/new-profile.jpg";

        // when
        user.updateSocialInfo(socialProvider, socialId, profileImageUrl);

        // then
        assertThat(user.getSocialProvider()).isEqualTo(socialProvider);
        assertThat(user.getSocialId()).isEqualTo(socialId);
        assertThat(user.getProfileImageUrl()).isEqualTo(profileImageUrl);
    }

    @Test
    @DisplayName("사용자 정보 부분 업데이트 테스트 - 모든 필드")
    void updateUserInfoAllFields() {
        // given
        User user = User.createUser("테스트유저", "test@example.com", "password", "01012345678");
        String newName = "업데이트유저";
        String newPhone = "01087654321";
        String newPassword = "newPassword";

        // when
        user.updateUserInfo(newName, newPhone, newPassword);

        // then
        assertThat(user.getName()).isEqualTo(newName);
        assertThat(user.getPhone()).isEqualTo(newPhone);
        assertThat(user.getPassword()).isEqualTo(newPassword);
    }

    @Test
    @DisplayName("사용자 정보 부분 업데이트 테스트 - 이름만")
    void updateUserInfoNameOnly() {
        // given
        User user = User.createUser("테스트유저", "test@example.com", "password", "01012345678");
        String oldPhone = user.getPhone();
        String oldPassword = user.getPassword();
        String newName = "업데이트유저";

        // when
        user.updateUserInfo(newName, null, null);

        // then
        assertThat(user.getName()).isEqualTo(newName);
        assertThat(user.getPhone()).isEqualTo(oldPhone); // 변경 없음
        assertThat(user.getPassword()).isEqualTo(oldPassword); // 변경 없음
    }

    @Test
    @DisplayName("사용자 탈퇴 테스트")
    void withdraw() {
        // given
        User user = User.createUser("테스트유저", "test@example.com", "password", "01012345678");
        String originalEmail = user.getEmail();

        // when
        user.withdraw();

        // then
        assertThat(user.getWithdrawalYn()).isEqualTo("Y");
        assertThat(user.getName()).isEqualTo("탈퇴회원");
        assertThat(user.getPassword()).isNull();
        // 이메일 익명화 확인
        assertThat(user.getEmail()).contains("withdrawn_");
        assertThat(user.getEmail()).contains(originalEmail);
    }
} 