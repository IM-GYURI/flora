package plannery.flora.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseMessage {
  SUCCESS_SIGNUP("회원가입 완료"),
  SUCCESS_SIGNOUT("로그아웃 성공");

  private final String message;
}
