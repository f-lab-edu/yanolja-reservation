package com.yanolja.areas.user.domain;

import com.yanolja.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User extends BaseEntity{
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

    @Column(nullable = false)
    @Comment("비밀번호")
    private String password;

    @Column(nullable = false)
    @Comment("전화번호")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Comment("사용자 역할")
    private UserRole role = UserRole.USER;

    public void changeName(String name) {
        this.name = name;
    }

    public void changeEmail(String email) {
        this.email = email;
    }

    public void changePassword(String password) {
        this.password = password;
    }

    public void changePhone(String phone) {
        this.phone = phone;
    }

    public void changeRole(UserRole role) {
        this.role = role;
    }
}