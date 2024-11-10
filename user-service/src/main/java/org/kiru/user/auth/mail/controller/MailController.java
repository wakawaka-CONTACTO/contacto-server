package org.kiru.user.auth.mail.controller;

import jakarta.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import lombok.RequiredArgsConstructor;
import org.kiru.user.auth.mail.dto.MailCheckDto;
import org.kiru.user.auth.mail.dto.MailSendDto;
import org.kiru.user.auth.mail.service.MailService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class MailController {

    private final MailService mailService;

    @PostMapping("/emailsend") // 이 부분은 각자 바꿔주시면 됩니다.
    public String emailCheck(@RequestBody MailSendDto mailSendDTO) throws UnsupportedEncodingException, MessagingException {
        String authCode = mailService.sendSimpleMessage(mailSendDTO.getEmail());
        return authCode; // Response body에 값을 반환
    }

    @PostMapping("/emailcheck") // 이 부분은 각자 바꿔주시면 됩니다.
    public Boolean emailCheck(@RequestBody MailCheckDto mailCheckDto) throws UnsupportedEncodingException, MessagingException {
        boolean check  = mailService.checkMessage(mailCheckDto);
        return check;
    }
}
