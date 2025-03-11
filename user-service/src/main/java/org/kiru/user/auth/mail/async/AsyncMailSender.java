package org.kiru.user.auth.mail.async;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncMailSender {

  private final RedisTemplate<String, String> redisTemplateForOne;
  private final JavaMailSender javaMailSender;
  private static ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

  public void sendMail(MimeMessage message, String number) throws MessagingException {
    String received = message.getRecipients(MimeMessage.RecipientType.TO)[0].toString();

    try {
      CompletableFuture.runAsync(() -> {
        javaMailSender.send(message);
      }, executor);
      CompletableFuture.runAsync(() -> {
        redisTemplateForOne.opsForValue().set(received, number);
      }, executor);
    } catch (MailException e) {
      log.error(e.getMessage());
      throw new IllegalArgumentException("메일 발송 중 오류가 발생했습니다.");
    }
  }
}
