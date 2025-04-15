package org.kiru.user.auth.mail.async;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncMailSender {

  private final RedisTemplate<String, String> redisTemplateForOne;
  private final JavaMailSender javaMailSender;

  @Async("virtualThreadExecutor")
  public void sendMail(MimeMessage message, String number) throws MessagingException {
    try {
      javaMailSender.send(message);
    } catch (MailException e) {
      log.error("Error sending mail: {}", e.getMessage(), e);
      throw e;
    }
  }

  @Async("virtualThreadExecutor")
  public void addNumberToRedis(String received, String number){
    try {
      redisTemplateForOne.opsForValue().set(received, number);
    } catch (Exception e) {
      log.error("Error caching mail data: {}", e.getMessage(), e);
      throw e;
    }
  }
}
