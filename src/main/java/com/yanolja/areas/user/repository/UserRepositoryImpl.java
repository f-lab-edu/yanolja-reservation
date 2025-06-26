package com.yanolja.areas.user.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yanolja.areas.user.domain.User;
import com.yanolja.areas.user.domain.QUser;
import com.yanolja.areas.user.dto.UserSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<User> searchUsers(UserSearchCondition condition) {
        QUser user = QUser.user;
        var q = queryFactory.selectFrom(user);
        q.where(user.withdrawalYn.eq("N")); // 탈퇴하지 않은 회원만 조회
        
        if (condition != null) {
            if (StringUtils.hasText(condition.getName())) {
                q.where(user.name.containsIgnoreCase(condition.getName()));
            }
            if (StringUtils.hasText(condition.getEmail())) {
                q.where(user.email.eq(condition.getEmail()));
            }
            if (StringUtils.hasText(condition.getPhone())) {
                q.where(user.phone.eq(condition.getPhone()));
            }
            if (condition.getCreatedAtFrom() != null) {
                q.where(user.createdAt.goe(condition.getCreatedAtFrom()));
            }
            if (condition.getCreatedAtTo() != null) {
                q.where(user.createdAt.loe(condition.getCreatedAtTo()));
            }
            if (condition.getRole() != null) {
                q.where(user.role.eq(condition.getRole()));
            }
            if (condition.getSocialProvider() != null) {
                q.where(user.socialProvider.eq(condition.getSocialProvider()));
            }
        }
        
        return q.fetch();
    }
    
    /**
     * 이메일로 탈퇴하지 않은 회원 조회
     */
    @Override
    public Optional<User> findActiveUserByEmail(String email) {
        QUser user = QUser.user;
        User result = queryFactory
                .selectFrom(user)
                .where(user.email.eq(email)
                        .and(user.withdrawalYn.eq("N")))
                .fetchOne();
        return Optional.ofNullable(result);
    }
    
    /**
     * ID로 탈퇴하지 않은 회원 조회
     */
    @Override
    public Optional<User> findActiveUserById(Long userId) {
        QUser user = QUser.user;
        User result = queryFactory
                .selectFrom(user)
                .where(user.id.eq(userId)
                        .and(user.withdrawalYn.eq("N")))
                .fetchOne();
        return Optional.ofNullable(result);
    }
    
    /**
     * 탈퇴 여부와 관계없이 이메일로 사용자 조회
     */
    @Override
    public Optional<User> findUserByEmail(String email) {
        QUser user = QUser.user;
        User result = queryFactory
                .selectFrom(user)
                .where(user.email.eq(email))
                .fetchOne();
        return Optional.ofNullable(result);
    }
} 