package org.kiru.user.auth.mail.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.core.exception.ConflictException;
import org.kiru.core.exception.code.FailureCode;
import org.kiru.user.auth.mail.async.AsyncMailSender;
import org.kiru.user.auth.mail.dto.MailCheckDto;
import org.kiru.user.auth.mail.dto.MailSendDto;
import org.kiru.user.auth.mail.enums.EmailSendPurpose;
import org.kiru.user.user.service.UserService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {

  private final JavaMailSender javaMailSender;
  private final UserService userService;
  private final RedisTemplate<String, String> redisTemplateForOne;
  private final AsyncMailSender asyncMailSender;

  // 랜덤으로 6자리 숫자 생성
  public String createNumber() {
    Random random = new Random();
    StringBuilder key = new StringBuilder();
    for (int i = 0; i < 6; i++) {
      key.append(random.nextInt(10));
    }
    return key.toString();
  }

  private void validateEmailNotExists(String email) {
    if (userService.existsByEmail(email)) {
      throw new ConflictException(FailureCode.DUPLICATE_EMAIL);
    }
  }

  // 메일 발송
  public void sendSimpleMessage(MailSendDto sendEmail) {
    String address = sendEmail.getEmail();
    if(sendEmail.getPurpose() == EmailSendPurpose.SIGNUP){
      validateEmailNotExists(address);
    }
    String number = createNumber(); // 랜덤 인증번호 생성
    asyncMailSender.addNumberToRedis(address, number);
    asyncMailSender.sendMail(address, number);
  }

  public Boolean checkMessage(MailCheckDto mailCheckDto) {
    String savedNumber = redisTemplateForOne.opsForValue().get(mailCheckDto.getEmail());
    if (savedNumber != null && savedNumber.equals(mailCheckDto.getAuthCode())) {
      return true;
    }
    return false;
  }
}