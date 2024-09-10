package plannery.flora.service;

import static plannery.flora.enums.ImageType.IMAGE_GALLERY;
import static plannery.flora.enums.ImageType.IMAGE_PROFILE;
import static plannery.flora.enums.UserRole.ROLE_MEMBER;
import static plannery.flora.exception.ErrorCode.PASSWORD_NOT_MATCH;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import plannery.flora.entity.MemberEntity;
import plannery.flora.exception.CustomException;
import plannery.flora.repository.MemberRepository;
import plannery.flora.security.JwtTokenProvider;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final ImageService imageService;
  private final BlacklistTokenService blacklistTokenService;

  /**
   * 회원가입 & 로그인
   *
   * @param email    이메일
   * @param password 비밀번호
   * @return JWT 토큰
   */
  public String signUpOrSignIn(String email, String password) {
    // 이메일을 통해 회원이 이미 존재하는지 확인
    Optional<MemberEntity> existingMember = memberRepository.findByEmail(email);

    if (existingMember.isPresent()) {
      // 회원이 존재하는 경우 -> 로그인
      MemberEntity member = existingMember.get();

      if (passwordEncoder.matches(password, member.getPassword())) {
        // 비밀번호가 일치하는 경우 : 로그인 성공
        log.info("로그인 성공");
        return jwtTokenProvider.generateToken(member.getId(), member.getEmail(),
            member.getRole());
      } else {
        // 비밀번호가 일치하지 않는 경우 : 로그인 실패
        log.info("로그인 실패");
        throw new CustomException(PASSWORD_NOT_MATCH);
      }
    } else {
      // 회원이 존재하지 않는 경우 -> 회원가입
      log.info("회원가입 성공");
      MemberEntity newMember = MemberEntity.builder()
          .email(email)
          .password(passwordEncoder.encode(password))
          .role(ROLE_MEMBER)
          .build();

      memberRepository.save(newMember);

      imageService.createDefaultImage(newMember.getId(), IMAGE_PROFILE);
      imageService.createDefaultImage(newMember.getId(), IMAGE_GALLERY);

      return jwtTokenProvider.generateToken(newMember.getId(), newMember.getEmail(),
          newMember.getRole());
    }
  }

  /**
   * 로그아웃
   *
   * @param token 토큰 정보
   */
  public void signOut(String token) {
    blacklistTokenService.addToBlacklist(token);
  }
}
