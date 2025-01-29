package org.kiru.user.admin.dto;

public record AdminUserDto(
        UserDto userDto,
        Boolean isConnected
) {
    public record UserDto(
            Long id,
            String name,
            String portfolioImageUrl
    ) {
    }
    public static AdminUserDto of(UserDto userDto, Boolean isConnected) {
        return new AdminUserDto(userDto, isConnected);
    }
}
