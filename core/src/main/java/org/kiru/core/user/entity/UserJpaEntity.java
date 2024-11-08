package org.kiru.core.user.entity;

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
import org.kiru.core.user.domain.LoginType;
import org.kiru.core.user.domain.User;

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
    private  LoginType loginType;

    @Column(name = "email")
    @Email
    private String email;

    @Column(name = "description",length = 1024)
    private String description;

    @Column(name = "instagram_id",nullable = false)
    private String instagramId;

    @Column(name = "web_url",nullable = false)
    private String webUrl;

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
                .build();
    }
}