package org.kiru.user.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.kiru.core.user.user.domain.LoginType;


public record UserSignUpReq(
        @NotBlank(message = "이름은 필수 입력 항목입니다.")
        String name,
        LoginType loginType,
        String socialId,
        @NotBlank(message = "이메일은 필수 입력 항목입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,
        @NotBlank(message = "설명은 필수 입력 항목입니다.")
        String description,
        @NotBlank(message = "인스타그램 아이디는 필수 입력 항목입니다.")
        String instagramId,
        String webUrl,
        @NotNull(message = "비밀번호는 필수 입력 항목입니다.")
        String password
) {
}
