package org.kiru.user.auth.mail.async;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
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
  private static final String senderEmail = "rlarlgnszx0319@gmail.com";

  public void sendMail(String address, String number) {
    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()){
      MimeMessage message = createMail(address, number);
      executor.submit( () -> javaMailSender.send(message) );
    } catch (MessagingException e) {
      log.error("Error sending mail: {}", e.getMessage(), e);
    } catch (MailException e) {
      log.error("Error sending mail: {}", e.getMessage(), e);
      throw e;
    }
  }

  public void addNumberToRedis(String received, String number){
    redisTemplateForOne.opsForValue().set(received, number);
  }

  private MimeMessage createMail(String mail, String number) throws MessagingException {
    MimeMessage message = javaMailSender.createMimeMessage();
    message.setFrom(senderEmail);
    message.setRecipients(MimeMessage.RecipientType.TO, mail);
    message.setSubject("이메일 인증");
    String body = "";
    body += "<h3>요청하신 인증 번호입니다.</h3>";
    body += "<h1>" + number + "</h1>";
    body += "<h3>감사합니다.</h3>";
    message.setText(body, "UTF-8", "html");
    return message;
  }
}
