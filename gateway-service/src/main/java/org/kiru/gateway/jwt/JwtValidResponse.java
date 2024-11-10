package org.kiru.gateway.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.kiru.core.user.user.entity.UserJpaEntity;
import org.kiru.gateway.jwt.JwtValidationType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class JwtValidResponse {
    private JwtValidationType status;
    private UserJpaEntity user;

    public static JwtValidResponse of(JwtValidationType status) {
        return new JwtValidResponse(status, null);
    }

    public static JwtValidResponse of(UserJpaEntity user) {
        return new JwtValidResponse(JwtValidationType.VALID_JWT, user);
    }
}
