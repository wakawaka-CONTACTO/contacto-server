package org.kiru.user.auth.mail.controller;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.kiru.user.auth.mail.dto.MailCheckDto;
import org.kiru.user.auth.mail.dto.MailCheckResponse;
import org.kiru.user.auth.mail.dto.MailSendDto;
import org.kiru.user.auth.mail.dto.MailSendResponse;
import org.kiru.user.auth.mail.service.MailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class MailController {

    private final MailService mailService;

    @PostMapping("/emailsend")
    public ResponseEntity<MailSendResponse> emailCheck(@RequestBody MailSendDto mailSendDTO) throws MessagingException {
        return ResponseEntity.ok(new MailSendResponse(mailService.sendSimpleMessage(mailSendDTO.getEmail()))); // Response body에 값을 반환
    }

    @PostMapping("/emailcheck")
    public ResponseEntity<MailCheckResponse> emailCheck(@RequestBody MailCheckDto mailCheckDto) {
        return ResponseEntity.ok(new MailCheckResponse(mailService.checkMessage(mailCheckDto)));
    }
}
