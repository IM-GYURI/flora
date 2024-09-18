package plannery.flora.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeDto {

  @NotBlank(message = "현재 비밀번호는 필수 입력값입니다.")
  private String oldPassword;

  @NotBlank(message = "새 비밀번호는 필수 입력값입니다.")
  @Pattern(
      regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|])[A-Za-z\\d~!@#$%^&*()+|]{8,16}$",
      message = "비밀번호는 8~16자 영문 대 소문자, 숫자, 특수문자를 각각 하나씩 포함해야 합니다."
  )
  private String newPassword;
}
