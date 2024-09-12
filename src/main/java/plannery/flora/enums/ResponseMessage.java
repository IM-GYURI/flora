package plannery.flora.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseMessage {
  SUCCESS_SIGNUP("회원가입 완료"),
  SUCCESS_SIGNOUT("로그아웃 완료"),
  NO_IMAGE_FILE("이미지 파일 부재"),
  SUCCESS_IMAGE_DELETE("이미지 파일 삭제 완료"),
  SUCCESS_PASSWORD_CHANGE("비밀번호 변경 완료"),
  SUCCESS_SEND_PASSWORD_CHANGE("비밀번호 변경 링크 전송 완료"),
  SUCCESS_MEMBER_DELETE("회원 탈퇴 완료");

  private final String message;
}
