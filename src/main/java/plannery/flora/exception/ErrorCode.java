package plannery.flora.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
  MEMBER_NOT_FOUND(404, "가입된 회원을 찾을 수 없습니다."),
  INVALID_MEMBER_ID(401, "유효하지 않은 회원 아이디입니다."),
  PASSWORD_NOT_MATCH(400, "비밀번호가 일치하지 않습니다."),
  INVALID_TOKEN(401, "유효하지 않은 토큰입니다.");

  private final int status;
  private final String message;
}
