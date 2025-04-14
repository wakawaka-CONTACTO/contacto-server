package org.kiru.core.user.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.kiru.core.user.common.BaseTimeEntity;
import org.kiru.core.user.user.domain.LoginType;
import org.kiru.core.user.user.domain.Nationality;
import org.kiru.core.user.user.domain.User;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Table(name = "users",
        indexes = {@Index(name = "idx_username", columnList = "username"),
                @Index(name = "idx_email", columnList = "email")})
@SQLDelete(sql = "UPDATE users SET deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted = false")
public class UserJpaEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username")
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "nationality", nullable = false)
    private Nationality nationality;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_type")
    private LoginType loginType;

    @Column(name = "email", unique = true)
    @Email
    private String email;

    @Column(name = "description", length = 1024)
    private String description;

    @Column(name = "instagram_id", nullable = false)
    private String instagramId;

    @Column(name = "web_url")
    private String webUrl;

    @Column(name = "password")
    @Setter
    private String password;

    @Column(name = "deleted")
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static UserJpaEntity of(User user) {
        return UserJpaEntity.builder()
                .id(user.getId())
                .username(user.getUsername())
                .loginType(user.getLoginType())
                .nationality(user.getNationality())
                .email(user.getEmail())
                .description(user.getDescription())
                .instagramId(user.getInstagramId())
                .webUrl(user.getWebUrl())
                .password(user.getPassword())
                .build();
    }

    public static User toModel(UserJpaEntity entity) {
        return User.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .loginType(entity.getLoginType())
                .nationality(entity.getNationality())
                .email(entity.getEmail())
                .description(entity.getDescription())
                .instagramId(entity.getInstagramId())
                .webUrl(entity.getWebUrl())
                .password(entity.getPassword())
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
        if (user.getLoginType() != null) {
            this.loginType = user.getLoginType();
        }
        if (user.getNationality() != null) {
            this.nationality = user.getNationality();
        }
    }
}