package plannery.flora.service;

import static plannery.flora.exception.ErrorCode.FAIL_EMAIL_SEND;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import plannery.flora.exception.CustomException;

@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String from;

  /**
   * 임시 비밀번호 전송
   *
   * @param to             받는 사람 이메일
   * @param randomPassword 임시 비밀번호
   */
  public void sendPasswordChangeEmail(String to, String randomPassword) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setTo(to);
      helper.setFrom(from);
      helper.setSubject("플로라 : 임시 비밀번호 발급");

      String htmlContent =
          "<p>새로운 비밀번호를 발급하였습니다. 회원님의 임시 비밀번호는 <b>" + randomPassword + "</b>입니다.</p>";

      helper.setText(htmlContent, true);

      mailSender.send(message);
    } catch (MessagingException e) {
      throw new CustomException(FAIL_EMAIL_SEND);
    }
  }
}
