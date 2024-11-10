package org.kiru.user.auth.mail.dto;

import lombok.Data;

@Data
public class MailCheckDto {
    private String email;
    private String authCode;
}
