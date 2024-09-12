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
   * 비밀번호 변경 url 전송
   *
   * @param to       받는 사람 이메일
   * @param resetUrl 비밀번호 변경 url
   */
  public void sendPasswordChangeEmail(String to, String resetUrl) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setTo(to);
      helper.setFrom(from);
      helper.setSubject("플로라 : 비밀번호 변경");

      String htmlContent = "<p>아래 링크를 통해 비밀번호를 변경하세요:</p>" +
          "<p><a href=\"" + resetUrl + "\">비밀번호 변경 링크</a></p>" +
          "<p>이 링크는 5분 동안만 유효합니다. 시간이 지나면 링크가 만료되어 사용할 수 없으니, 가능한 빨리 비밀번호를 변경해 주세요.</p>";

      helper.setText(htmlContent, true);

      mailSender.send(message);
    } catch (MessagingException e) {
      throw new CustomException(FAIL_EMAIL_SEND);
    }
  }
}
