package plannery.flora.service;

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

      return jwtTokenProvider.generateToken(newMember.getId(), newMember.getEmail(),
          newMember.getRole());
    }
  }
}
