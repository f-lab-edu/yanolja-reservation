package com.yanolja.areas.user.repository;

import com.yanolja.areas.user.domain.User;
import com.yanolja.areas.user.dto.UserSearchCondition;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryCustom {
    List<User> searchUsers(UserSearchCondition condition);
    Optional<User> findActiveUserByEmail(String email);
    Optional<User> findActiveUserById(Long userId);
}
