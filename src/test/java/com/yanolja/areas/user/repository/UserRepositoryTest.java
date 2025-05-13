package com.yanolja.areas.user.repository;

import com.yanolja.areas.user.config.TestAuditorAwareConfig;
import com.yanolja.areas.user.domain.SocialProvider;
import com.yanolja.areas.user.domain.User;
import com.yanolja.areas.user.domain.UserRole;
import com.yanolja.areas.user.dto.UserSearchCondition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({UserRepositoryConfig.class, TestAuditorAwareConfig.class})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("일반 사용자 저장 및 조회 테스트")
    void saveAndFindUser() {
        // given
        User user = User.createUser("테스트유저", "test@example.com", "encodedPassword", "010-1234-5678");

        // when
        User savedUser = userRepository.save(user);
        User foundUser = userRepository.findById(savedUser.getId()).orElse(null);

        // then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.getName()).isEqualTo("테스트유저");
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("이메일로 활성 사용자 조회 테스트")
    void findActiveUserByEmail() {
        // given
        User user = User.createUser("테스트유저", "test@example.com", "encodedPassword", "010-1234-5678");
        userRepository.save(user);

        // when
        Optional<User> foundUserOptional = userRepository.findActiveUserByEmail("test@example.com");

        // then
        assertThat(foundUserOptional).isPresent();
        User foundUser = foundUserOptional.get();
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 활성 사용자 조회 테스트")
    void findActiveUserByEmail_NotExist() {
        // when
        Optional<User> foundUserOptional = userRepository.findActiveUserByEmail("nonexistent@example.com");

        // then
        assertThat(foundUserOptional).isEmpty();
    }

    @Test
    @DisplayName("탈퇴 사용자는 findActiveUserByEmail로 조회되지 않음")
    void findActiveUserByEmail_WithdrawnUser() {
        // given
        User user = User.createUser("테스트유저", "test@example.com", "encodedPassword", "010-1234-5678");
        userRepository.save(user);
        user.withdraw();
        userRepository.save(user);

        // when
        Optional<User> foundUserOptional = userRepository.findActiveUserByEmail("test@example.com");

        // then
        assertThat(foundUserOptional).isEmpty();
    }

    @Test
    @DisplayName("ID로 활성 사용자 조회 테스트")
    void findActiveUserById() {
        // given
        User user = User.createUser("테스트유저", "test@example.com", "encodedPassword", "010-1234-5678");
        User savedUser = userRepository.save(user);

        // when
        Optional<User> foundUserOptional = userRepository.findActiveUserById(savedUser.getId());

        // then
        assertThat(foundUserOptional).isPresent();
        User foundUser = foundUserOptional.get();
        assertThat(foundUser.getId()).isEqualTo(savedUser.getId());
    }

    @Test
    @DisplayName("존재하지 않는 ID로 활성 사용자 조회 테스트")
    void findActiveUserById_NotExist() {
        // when
        Optional<User> foundUserOptional = userRepository.findActiveUserById(999L);

        // then
        assertThat(foundUserOptional).isEmpty();
    }

    @Test
    @DisplayName("탈퇴 사용자는 findActiveUserById로 조회되지 않음")
    void findActiveUserById_WithdrawnUser() {
        // given
        User user = User.createUser("테스트유저", "test@example.com", "encodedPassword", "010-1234-5678");
        User savedUser = userRepository.save(user);
        user.withdraw();
        userRepository.save(user);

        // when
        Optional<User> foundUserOptional = userRepository.findActiveUserById(savedUser.getId());

        // then
        assertThat(foundUserOptional).isEmpty();
    }

    @Test
    @DisplayName("이름으로 사용자 검색 테스트")
    void searchUsers_ByName() {
        // given
        User user1 = User.createUser("테스트유저", "test1@example.com", "encodedPassword", "010-1234-5678");
        User user2 = User.createUser("다른유저", "test2@example.com", "encodedPassword", "010-5678-1234");
        userRepository.save(user1);
        userRepository.save(user2);

        UserSearchCondition condition = new UserSearchCondition();
        condition.setName("테스트");

        // when
        List<User> users = userRepository.searchUsers(condition);

        // then
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getName()).isEqualTo("테스트유저");
    }

    @Test
    @DisplayName("이메일로 사용자 검색 테스트")
    void searchUsers_ByEmail() {
        // given
        User user1 = User.createUser("테스트유저", "test1@example.com", "encodedPassword", "010-1234-5678");
        User user2 = User.createUser("다른유저", "test2@example.com", "encodedPassword", "010-5678-1234");
        userRepository.save(user1);
        userRepository.save(user2);

        UserSearchCondition condition = new UserSearchCondition();
        condition.setEmail("test1@example.com");

        // when
        List<User> users = userRepository.searchUsers(condition);

        // then
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getEmail()).isEqualTo("test1@example.com");
    }

    @Test
    @DisplayName("역할로 사용자 검색 테스트")
    void searchUsers_ByRole() {
        // given
        User user1 = User.createUser("일반유저", "user@example.com", "encodedPassword", "010-1234-5678");
        User user2 = User.createAdmin("관리자", "admin@example.com", "encodedPassword", "010-5678-1234");
        userRepository.save(user1);
        userRepository.save(user2);

        UserSearchCondition condition = new UserSearchCondition();
        condition.setRole(UserRole.ADMIN);

        // when
        List<User> users = userRepository.searchUsers(condition);

        // then
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(users.get(0).getEmail()).isEqualTo("admin@example.com");
    }

    @Test
    @DisplayName("소셜 제공자로 사용자 검색 테스트")
    void searchUsers_BySocialProvider() {
        // given
        User user1 = User.createSocialUser("구글유저", "google@example.com", SocialProvider.GOOGLE, "google123", null);
        User user2 = User.createSocialUser("카카오유저", "kakao@example.com", SocialProvider.KAKAO, "kakao123", null);
        userRepository.save(user1);
        userRepository.save(user2);

        UserSearchCondition condition = new UserSearchCondition();
        condition.setSocialProvider(SocialProvider.GOOGLE);

        // when
        List<User> users = userRepository.searchUsers(condition);

        // then
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getSocialProvider()).isEqualTo(SocialProvider.GOOGLE);
        assertThat(users.get(0).getEmail()).isEqualTo("google@example.com");
    }

    @Test
    @DisplayName("복합 조건으로 사용자 검색 테스트")
    void searchUsers_MultipleConditions() {
        // given
        User user1 = User.createUser("테스트유저1", "test1@example.com", "encodedPassword", "010-1234-5678");
        User user2 = User.createUser("테스트유저2", "test2@example.com", "encodedPassword", "010-5678-1234");
        userRepository.save(user1);
        userRepository.save(user2);

        UserSearchCondition condition = new UserSearchCondition();
        condition.setName("테스트");
        condition.setPhone("010-1234-5678");

        // when
        List<User> users = userRepository.searchUsers(condition);

        // then
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getName()).isEqualTo("테스트유저1");
        assertThat(users.get(0).getPhone()).isEqualTo("010-1234-5678");
    }
} 