package plannery.flora.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
  MEMBER_NOT_FOUND(404, "가입된 회원을 찾을 수 없습니다."),
  INVALID_MEMBER_ID(401, "유효하지 않은 회원 아이디입니다."),
  PASSWORD_NOT_MATCH(400, "비밀번호가 일치하지 않습니다."),
  INVALID_TOKEN(401, "유효하지 않은 토큰입니다."),
  NO_AUTHORITY(401, "권한이 없습니다."),
  BLACKLIST_TOKEN_ADD_FAILED(500, "블랙리스트 토큰 추가에 실패했습니다."),
  TOKEN_BLACKLISTED(401, "토큰이 블랙리스트에 존재하여 사용할 수 없습니다."),
  INVALID_FILE_FORMAT(400, "지원하지 않는 형식의 파일입니다."),
  FILE_SIZE_EXCEEDED(400, "최대 파일 크기를 넘습니다."),
  S3_UPLOAD_ERROR(500, "S3에 이미지를 업로드하는 중 오류가 발생했습니다."),
  INVALID_IMAGE_URL(400, "유효하지 않은 이미지 url입니다."),
  FAILED_TO_DELETE_IMAGE(500, "이미지 삭제에 실패했습니다."),
  IMAGE_NOT_FOUND(404, "이미지를 조회하지 못했습니다."),
  SAME_PASSWORD(400, "현재 비밀번호와 새 비밀번호가 같습니다."),
  FAIL_EMAIL_SEND(500, "이메일 전송에 실패했습니다."),
  PROMISE_NOT_FOUND(404, "다짐 내용이 존재하지 않습니다."),
  PROMISE_EXISTS(400, "다짐이 이미 존재합니다."),
  DIARY_NOT_FOUND(404, "해당 일기가 존재하지 않습니다."),
  DIARY_EXISTS(400, "해당 날짜에 일기가 이미 존재합니다."),
  EVENT_NOT_FOUND(404, "해당 이벤트가 존재하지 않습니다."),
  INVALID_DATETIME(400, "종료일시는 시작일시보다 앞설 수 없습니다."),
  FLORA_NOT_FOUND(404, "해당 회원의 플로라를 찾을 수 없습니다."),
  FLORA_EXISTS(400, "플로라가 이미 존재합니다.");

  private final int status;
  private final String message;
}
