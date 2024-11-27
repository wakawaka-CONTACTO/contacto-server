package org.kiru.core.user.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.kiru.core.user.user.domain.LoginType;
import org.kiru.core.user.user.domain.User;
import org.springframework.stereotype.Component;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Table(name = "users")
public class UserJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "social_id")
    private  String socialId;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_type")
    private LoginType loginType;

    @Column(name = "email", unique = true)
    @Email
    private String email;

    @Column(name = "description",length = 1024)
    private String description;

    @Column(name = "instagram_id",nullable = false)
    private String instagramId;

    @Column(name = "web_url",nullable = false)
    private String webUrl;

    @Column(name = "password", nullable = true)
    @Setter
    private String password;

    public static UserJpaEntity of(User user) {
        return UserJpaEntity.builder()
                .id(user.getId())
                .username(user.getUsername())
                .loginType(user.getLoginType())
                .socialId(user.getSocialId())
                .email(user.getEmail())
                .description(user.getDescription())
                .instagramId(user.getInstagramId())
                .webUrl(user.getWebUrl())
                .password(user.getPassword())
                .build();
    }

    public void updateDetails(User user) {
        if (user.getUsername() != null) {
            this.username = user.getUsername();
        }
        if (user.getEmail() != null) {
            this.email = user.getEmail();
        }
        if (user.getDescription() != null) {
            this.description = user.getDescription();
        }
        if (user.getInstagramId() != null) {
            this.instagramId = user.getInstagramId();
        }
        if (user.getWebUrl() != null) {
            this.webUrl = user.getWebUrl();
        }
        if (user.getPassword() != null) {
            this.password = user.getPassword();
        }
    }
}