package org.kiru.user.auth.mail.async;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
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
  private final Executor executor;

  @Async
  public void sendMail(MimeMessage message, String number) throws MessagingException {
    String received = message.getRecipients(MimeMessage.RecipientType.TO)[0].toString();
    CompletableFuture<Void> sendMailFuture = CompletableFuture.runAsync(() -> {
      try {
        javaMailSender.send(message);
      } catch (MailException e) {
        log.error("Error sending mail: {}", e.getMessage(), e);
        throw e;
      }
    }, executor);

    CompletableFuture<Void> cacheFuture = CompletableFuture.runAsync(() -> {
      try {
        redisTemplateForOne.opsForValue().set(received, number);
      } catch (Exception e) {
        log.error("Error caching mail data: {}", e.getMessage(), e);
        throw e;
      }
    }, executor);
  }
}
