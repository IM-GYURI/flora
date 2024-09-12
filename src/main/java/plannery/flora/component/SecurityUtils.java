package plannery.flora.component;

import static plannery.flora.exception.ErrorCode.MEMBER_NOT_FOUND;
import static plannery.flora.exception.ErrorCode.NO_AUTHORITY;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import plannery.flora.entity.MemberEntity;
import plannery.flora.exception.CustomException;
import plannery.flora.repository.MemberRepository;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

  private final MemberRepository memberRepository;

  /**
   * 본인 확인
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @return MemberEntity
   */
  public MemberEntity validateUserDetails(UserDetails userDetails, Long memberId) {
    MemberEntity member = memberRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

    if (!member.getId().equals(memberId)) {
      throw new CustomException(NO_AUTHORITY);
    }

    return member;
  }
}
