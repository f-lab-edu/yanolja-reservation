package com.yanolja.areas.user.service;

import com.yanolja.areas.user.dto.UserInfoResponse;
import com.yanolja.areas.user.repository.UserRepository;
import com.yanolja.areas.user.dto.UserUpdateRequest;
import com.yanolja.areas.user.dto.UserSearchCondition;
import com.yanolja.areas.user.domain.User;
import com.yanolja.common.exception.ErrorCode;
import com.yanolja.common.exception.UserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 사용자 정보 관리 서비스 (인터페이스 없이 구현체 단독)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** 사용자 단건 조회 */
    @Transactional(readOnly = true)
    public UserInfoResponse getUserById(Long userId) {
        User user = findUserOrThrow(userId);
        return UserInfoResponse.from(user);
    }

    /** Querydsl 동적 조건 검색 (UserCustom 사용) */
    @Transactional(readOnly = true)
    public List<UserInfoResponse> searchUsers(UserSearchCondition condition) {
        List<User> users = userRepository.searchUsers(condition);
        return users.stream().map(UserInfoResponse::from).collect(Collectors.toList());
    }

    /** 사용자 정보 수정 */
    @Transactional
    public void updateUser(Long userId, UserUpdateRequest request) {
        User user = findUserOrThrow(userId);
        validateUpdateRequest(request);

        String encryptedPassword = request.getPassword() != null ? 
            passwordEncoder.encode(request.getPassword()) : null;
        user.updateUserInfo(request.getName(), request.getPhone(), encryptedPassword);
        userRepository.save(user);
    }

    /** 회원 탈퇴 처리 */
    @Transactional
    public void withdrawUser(Long userId) {
        User user = findUserOrThrow(userId);
        user.withdraw();
        userRepository.save(user);
    }

    // 내부 함수
    private User findUserOrThrow(Long userId) {
        return userRepository.findActiveUserById(userId)
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 사용자: {}", userId);
                    return new UserException(ErrorCode.USER_NOT_FOUND);
                });
    }

    private void validateUpdateRequest(UserUpdateRequest request) {
        if (request == null) throw new UserException(ErrorCode.INVALID_INPUT_VALUE, "수정 요청이 비어 있습니다.");
        
        if (request.getName() != null && request.getName().length() < 2) {
            throw new UserException(ErrorCode.INVALID_INPUT_VALUE, "이름은 2자 이상이어야 합니다.");
        }
        
        if (request.getPhone() != null && !request.getPhone().matches("^01[0-9]-\\d{3,4}-\\d{4}$")) {
            throw new UserException(ErrorCode.INVALID_INPUT_VALUE, "전화번호 형식이 올바르지 않습니다.");
        }
        
        if (request.getPassword() != null && !request.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$")) {
            throw new UserException(ErrorCode.INVALID_PASSWORD, "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다.");
        }
    }

} 