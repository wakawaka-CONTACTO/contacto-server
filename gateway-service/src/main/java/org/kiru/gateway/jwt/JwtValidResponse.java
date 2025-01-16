package org.kiru.gateway.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.kiru.core.user.user.domain.User;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class JwtValidResponse {
    private JwtValidationType status;
    private String email;
    private Long userId;

    public static JwtValidResponse of(JwtValidationType status) {
        return new JwtValidResponse(status, null, null);
    }

    public static JwtValidResponse of(User user) {
        return new JwtValidResponse(JwtValidationType.VALID_JWT, user.getEmail(), user.getId());
    }
}
