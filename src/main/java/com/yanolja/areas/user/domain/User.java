package com.yanolja.areas.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("사용자 ID")
    private Long id;

    @Column(nullable = false)
    @Comment("사용자 이름")
    private String name;

    @Column(nullable = false, unique = true)
    @Comment("이메일")
    private String email;

    @Column
    @Comment("비밀번호")
    private String password;

    @Column
    @Comment("전화번호")
    private String phone;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Comment("사용자 역할")
    private UserRole role = UserRole.USER;
    
    @Column
    @Comment("소셜 로그인 제공자")
    @Enumerated(EnumType.STRING)
    private SocialProvider socialProvider;
    
    @Column
    @Comment("소셜 로그인 제공자 ID")
    private String socialId;
    
    @Column
    @Comment("프로필 이미지 URL")
    private String profileImageUrl;

    @Builder
    private User(String name, String email, String password, String phone, UserRole role, 
                SocialProvider socialProvider, String socialId, String profileImageUrl) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role != null ? role : UserRole.USER;
        this.socialProvider = socialProvider;
        this.socialId = socialId;
        this.profileImageUrl = profileImageUrl;
    }
    
    /**
     * 회원가입을 위한 사용자 생성
     * @param name 사용자 이름
     * @param email 이메일
     * @param encodedPassword 암호화된 비밀번호
     * @param phone 전화번호
     * @return 생성된 User 객체
     */
    public static User createUser(String name, String email, String encodedPassword, String phone) {
        return User.builder()
                .name(name)
                .email(email)
                .password(encodedPassword)
                .phone(phone)
                .role(UserRole.USER)
                .build();
    }
    
    /**
     * 소셜 로그인을 위한 사용자 생성
     * @param name 사용자 이름
     * @param email 이메일
     * @param socialProvider 소셜 로그인 제공자
     * @param socialId 소셜 로그인 제공자 ID
     * @param profileImageUrl 프로필 이미지 URL
     * @return 생성된 User 객체
     */
    public static User createSocialUser(String name, String email, SocialProvider socialProvider, 
                                       String socialId, String profileImageUrl) {
        return User.builder()
                .name(name)
                .email(email)
                .socialProvider(socialProvider)
                .socialId(socialId)
                .profileImageUrl(profileImageUrl)
                .role(UserRole.USER)
                .build();
    }
    
    /**
     * 관리자 사용자 생성
     * @param name 사용자 이름
     * @param email 이메일
     * @param encodedPassword 암호화된 비밀번호
     * @param phone 전화번호
     * @return 생성된 User 객체
     */
    public static User createAdmin(String name, String email, String encodedPassword, String phone) {
        return User.builder()
                .name(name)
                .email(email)
                .password(encodedPassword)
                .phone(phone)
                .role(UserRole.ADMIN)
                .build();
    }

    /**
     * 비밀번호 변경
     * @param password 새 비밀번호
     */
    public void changePassword(String password) {
        this.password = password;
    }
    
    /**
     * 소셜 로그인 정보 업데이트
     * @param socialProvider 소셜 로그인 제공자
     * @param socialId 소셜 로그인 제공자 ID
     */
    public void updateSocialInfo(SocialProvider socialProvider, String socialId, String profileImageUrl) {
        this.socialProvider = socialProvider;
        this.socialId = socialId;
        this.profileImageUrl = profileImageUrl;
    }
}