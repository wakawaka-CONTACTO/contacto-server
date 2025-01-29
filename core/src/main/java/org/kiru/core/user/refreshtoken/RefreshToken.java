package org.kiru.core.user.refreshtoken;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Table(name = "refresh_token")
public class RefreshToken  {
    @Id
    @Column(name = "refresh_token_id")
    private String token;

    @Column(name = "user_id", unique = true)
    private Long userId;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    public static RefreshToken create(final String token, final Long userId, LocalDateTime expiredAt) {
        return RefreshToken.builder()
                .token(token)
                .userId(userId)
                .expiredAt(expiredAt)
                .build();
    }
}
