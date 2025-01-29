package org.kiru.user.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import org.kiru.core.user.user.domain.LoginType;

@Builder
@NotEmpty(message = "userSignUpReq가 필요합니다")
public record UserSignUpReq(
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
                message = "비밀번호는 8자 이상이며, 영문자, 숫자, 특수문자를 포함해야 합니다")
        String password,

        @NotBlank(message = "이름은 필수입니다")
        @Pattern(regexp = "^[A-Za-z0-9가-힣]{2,20}$",
                message = "이름은 2-20자의 영문자, 숫자, 한글만 가능합니다")
        String name,
        String socialId,
        String webUrl,
        String description,
        String instagramId,
        LoginType loginType
) {
}
