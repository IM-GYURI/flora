package plannery.flora.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseMessage {
  SUCCESS_SIGNUP("회원가입 완료");

  private final String message;
}
