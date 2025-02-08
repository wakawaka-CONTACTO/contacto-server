package org.kiru.gateway.jwt;

import org.kiru.core.exception.EntityNotFoundException;
import org.kiru.core.exception.code.FailureCode;
import org.kiru.core.user.user.domain.User;
import org.kiru.core.user.user.entity.UserR2dbcEntity;

public record JwtValidStatusDto(JwtValidationType status, String email, Long userId) {
    public static JwtValidStatusDto of(JwtValidationType status) {
        return new JwtValidStatusDto(status, null, null);
    }

    public static JwtValidStatusDto of(User user) {
        return new JwtValidStatusDto(JwtValidationType.VALID_JWT, user.getEmail(), user.getId());
    }

    public static JwtValidStatusDto of(UserR2dbcEntity userR2dbcEntity) {
        if (userR2dbcEntity == null) {
            throw new EntityNotFoundException(FailureCode.USER_NOT_FOUND);
        }
        return new JwtValidStatusDto(JwtValidationType.VALID_JWT, userR2dbcEntity.getEmail(), userR2dbcEntity.getId());
    }
}