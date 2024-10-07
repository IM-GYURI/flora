package plannery.flora.dto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import plannery.flora.enums.UserRole;

@Getter
@AllArgsConstructor
public class MemberTokenInfoDto {

  private Long memberId;
  private String email;
  private UserRole role;
}
