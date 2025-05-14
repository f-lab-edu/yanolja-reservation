package com.yanolja.areas.user.service;

import com.yanolja.areas.auth.dto.RegisterRequest;
import com.yanolja.areas.user.domain.User;
import com.yanolja.areas.user.dto.UserInfoResponse;
import com.yanolja.areas.user.dto.UserSearchCondition;
import com.yanolja.areas.user.dto.UserUpdateRequest;
import com.yanolja.areas.user.repository.UserRepository;
import com.yanolja.common.exception.ErrorCode;
import com.yanolja.common.exception.UserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;
    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, passwordEncoder);
        
        // 테스트용 User 객체 생성
        mockUser = User.createUser(
            "테스트유저", 
            "test@example.com", 
            "encodedPassword", 
            "010-1234-5678"
        );
    }

    @Test
    @DisplayName("정상적인 회원가입 테스트")
    void registerUser_Success() {
        // given
        RegisterRequest request = new RegisterRequest("테스트유저", "test@example.com", "Test1234!", "010-1234-5678");
        
        when(userRepository.findActiveUserByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Test1234!")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // when
        User result = userService.registerUser(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        
        verify(userRepository).findActiveUserByEmail("test@example.com");
        verify(passwordEncoder).encode("Test1234!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("중복 이메일로 회원가입 시도시 실패")
    void registerUser_DuplicateEmail_Fail() {
        // given
        RegisterRequest request = new RegisterRequest("테스트유저", "test@example.com", "Test1234!", "010-1234-5678");
        
        when(userRepository.findActiveUserByEmail("test@example.com")).thenReturn(Optional.of(mockUser));

        // when & then
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("이미 사용 중인 이메일입니다");
        
        verify(userRepository).findActiveUserByEmail("test@example.com");
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("사용자 정보 조회 테스트")
    void getUserById_Success() {
        // given
        when(userRepository.findActiveUserById(1L)).thenReturn(Optional.of(mockUser));

        // when
        UserInfoResponse result = userService.getUserById(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회시 실패")
    void getUserById_NotFound_Fail() {
        // given
        when(userRepository.findActiveUserById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(UserException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("사용자 정보 수정 테스트")
    void updateUser_Success() {
        // given
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setName("수정된이름");
        updateRequest.setPhone("010-9876-5432");
        
        when(userRepository.findActiveUserById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // when
        userService.updateUser(1L, updateRequest);

        // then
        verify(userRepository).findActiveUserById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자 정보 수정시 실패")
    void updateUser_NotFound_Fail() {
        // given
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setName("수정된이름");
        
        when(userRepository.findActiveUserById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.updateUser(999L, updateRequest))
                .isInstanceOf(UserException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("회원 탈퇴 테스트")
    void withdrawUser_Success() {
        // given
        when(userRepository.findActiveUserById(1L)).thenReturn(Optional.of(mockUser));

        // when
        userService.withdrawUser(1L);

        // then
        verify(userRepository).findActiveUserById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자 탈퇴시 실패")
    void withdrawUser_NotFound_Fail() {
        // given
        when(userRepository.findActiveUserById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.withdrawUser(999L))
                .isInstanceOf(UserException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("사용자 검색 테스트")
    void searchUsers_Success() {
        // given
        UserSearchCondition condition = new UserSearchCondition();
        condition.setName("테스트");
        
        List<User> userList = Arrays.asList(
            mockUser,
            User.createUser("테스트유저2", "test2@example.com", "encodedPassword2", "010-8765-4321")
        );
        
        when(userRepository.searchUsers(any(UserSearchCondition.class))).thenReturn(userList);

        // when
        List<UserInfoResponse> results = userService.searchUsers(condition);

        // then
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(2);
    }
} 