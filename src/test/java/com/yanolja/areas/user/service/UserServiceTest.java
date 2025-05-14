package com.yanolja.areas.user.service;

import com.yanolja.areas.auth.dto.RegisterRequest;
import com.yanolja.areas.user.config.TestAuditorAwareConfig;
import com.yanolja.areas.user.domain.User;
import com.yanolja.areas.user.dto.UserInfoResponse;
import com.yanolja.areas.user.dto.UserSearchCondition;
import com.yanolja.areas.user.dto.UserUpdateRequest;
import com.yanolja.areas.user.repository.UserRepository;
import com.yanolja.common.exception.UserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Import(TestAuditorAwareConfig.class)
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private RegisterRequest validRegisterRequest;

    @BeforeEach
    void setUp() {
        validRegisterRequest = RegisterRequest.builder()
                .name("테스트유저")
                .email("test@example.com")
                .password("Test1234!")
                .phone("010-1234-5678")
                .build();
    }

    @Test
    @DisplayName("정상적인 회원가입 테스트")
    void registerUser_Success() {
        // when
        User registeredUser = userService.registerUser(validRegisterRequest);

        // then
        assertThat(registeredUser).isNotNull();
        assertThat(registeredUser.getEmail()).isEqualTo(validRegisterRequest.getEmail());
        assertThat(registeredUser.getName()).isEqualTo(validRegisterRequest.getName());
        assertThat(registeredUser.getPhone()).isEqualTo(validRegisterRequest.getPhone());
        assertThat(passwordEncoder.matches(validRegisterRequest.getPassword(), registeredUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("중복 이메일로 회원가입 시도시 실패")
    void registerUser_DuplicateEmail_Fail() {
        // given
        userService.registerUser(validRegisterRequest);

        // when & then
        assertThatThrownBy(() -> userService.registerUser(validRegisterRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("이미 사용 중인 이메일입니다.");
    }

    @Test
    @DisplayName("사용자 정보 조회 테스트")
    void getUserById_Success() {
        // given
        User registeredUser = userService.registerUser(validRegisterRequest);

        // when
        UserInfoResponse foundUser = userService.getUserById(registeredUser.getId());

        // then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo(validRegisterRequest.getEmail());
        assertThat(foundUser.getName()).isEqualTo(validRegisterRequest.getName());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회시 실패")
    void getUserById_NotFound_Fail() {
        // when & then
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(UserException.class);
    }

    @Test
    @DisplayName("사용자 정보 수정 테스트")
    void updateUser_Success() {
        // given
        User registeredUser = userService.registerUser(validRegisterRequest);
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setName("수정된이름");
        updateRequest.setPhone("010-9876-5432");
        updateRequest.setPassword("NewPass123!");

        // when
        userService.updateUser(registeredUser.getId(), updateRequest);

        // then
        UserInfoResponse updatedUser = userService.getUserById(registeredUser.getId());
        assertThat(updatedUser.getName()).isEqualTo(updateRequest.getName());
        assertThat(updatedUser.getPhone()).isEqualTo(updateRequest.getPhone());
    }

    @Test
    @DisplayName("회원 탈퇴 테스트")
    void withdrawUser_Success() {
        // given
        User registeredUser = userService.registerUser(validRegisterRequest);

        // when
        userService.withdrawUser(registeredUser.getId());

        // then
        assertThatThrownBy(() -> userService.getUserById(registeredUser.getId()))
                .isInstanceOf(UserException.class);
    }

    @Test
    @DisplayName("사용자 검색 테스트")
    void searchUsers_Success() {
        // given
        userService.registerUser(validRegisterRequest);
        UserSearchCondition condition = new UserSearchCondition();
        condition.setName("테스트");

        // when
        List<UserInfoResponse> users = userService.searchUsers(condition);

        // then
        assertThat(users).isNotEmpty();
        assertThat(users.get(0).getName()).contains("테스트");
    }
} 