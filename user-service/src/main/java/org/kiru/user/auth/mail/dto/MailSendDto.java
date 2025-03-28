package org.kiru.user.auth.mail.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kiru.user.auth.mail.enums.EmailSendPurpose;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MailSendDto {
    private String email;
    private EmailSendPurpose purpose;
}
