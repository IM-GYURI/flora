package plannery.flora.service;

import static plannery.flora.enums.ImageType.IMAGE_PROFILE;
import static plannery.flora.enums.UserRole.ROLE_ADMIN;
import static plannery.flora.enums.UserRole.ROLE_MEMBER;
import static plannery.flora.exception.ErrorCode.MEMBER_NOT_FOUND;
import static plannery.flora.exception.ErrorCode.NO_AUTHORITY;
import static plannery.flora.exception.ErrorCode.PASSWORD_NOT_MATCH;
import static plannery.flora.exception.ErrorCode.SAME_PASSWORD;
import static plannery.flora.util.RandomGenerator.generateTemporaryPassword;

import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plannery.flora.component.S3ImageUpload;
import plannery.flora.dto.member.MemberInfoDto;
import plannery.flora.dto.member.PasswordChangeDto;
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
  private final S3ImageUpload s3ImageUpload;
  private final ImageService imageService;
  private final EmailService emailService;
  private final FloraService floraService;
  private final NotificationService notificationService;
  private final BlacklistTokenService blacklistTokenService;

  private static final long TEMP_TOKEN_EXPIRATION_TIME = 300; // 5min

  /**
   * 회원가입 & 로그인 : 회원용
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

        floraService.updateFloraOnLogin(member.getId());

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
      imageService.createDefaultImage(newMember.getId());
      floraService.createMyFlora(newMember.getId());

      return jwtTokenProvider.generateToken(newMember.getId(), newMember.getEmail(),
          newMember.getRole());
    }
  }

  /**
   * 회원가입 & 로그인 : 관리자용
   *
   * @param email    이메일
   * @param password 비밀번호
   * @return JWT 토큰
   */
  public String signUpOrSignInForAdmin(String email, String password) {
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
          .role(ROLE_ADMIN)
          .build();

      memberRepository.save(newMember);

      return jwtTokenProvider.generateToken(newMember.getId(), newMember.getEmail(),
          newMember.getRole());
    }
  }

  /**
   * 로그아웃 : SSE 연결 해지
   *
   * @param token 토큰 정보
   */
  public void signOut(String token, Long memberId) {
    blacklistTokenService.addToBlacklist(token);
    notificationService.removeEmitter(memberId);
  }

  /**
   * 회원 정보 조회
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @return MemberInfoDto : 이메일, 프로필 이미지 URL
   */
  public MemberInfoDto getMemberInfo(UserDetails userDetails, Long memberId) {
    return MemberInfoDto.builder()
        .email(userDetails.getUsername())
        .imageUrl(imageService.getImage(userDetails, memberId, IMAGE_PROFILE))
        .build();
  }

  /**
   * 비밀번호 변경
   *
   * @param token             JWT 토큰
   * @param memberId          회원ID
   * @param passwordChangeDto : 현재 비밀번호, 새 비밀번호
   */
  @Transactional
  public void changePassword(String token, Long memberId,
      PasswordChangeDto passwordChangeDto) {
    Authentication authentication = jwtTokenProvider.getAuthentication(token);

    MemberEntity member = memberRepository.findByEmail(authentication.getName())
        .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

    if (!member.getId().equals(memberId)) {
      throw new CustomException(NO_AUTHORITY);
    }

    if (!passwordEncoder.matches(passwordChangeDto.getOldPassword(), member.getPassword())) {
      throw new CustomException(PASSWORD_NOT_MATCH);
    }

    if (passwordChangeDto.getOldPassword().equals(passwordChangeDto.getNewPassword())) {
      throw new CustomException(SAME_PASSWORD);
    }

    member.updatePassword(passwordEncoder.encode(passwordChangeDto.getNewPassword()));
  }

  /**
   * 비밀번호 찾기 -> 이메일로 임시 비밀번호 전송
   *
   * @param email 이메일
   */
  public void passwordChange(String email) {
    MemberEntity member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

    String temporaryPassword = generateTemporaryPassword();

    member.updatePassword(passwordEncoder.encode(temporaryPassword));
    memberRepository.save(member);

    emailService.sendPasswordChangeEmail(email, temporaryPassword);
  }

  /**
   * 임시 토큰 블랙리스트 등록 스케줄링 (5분)
   *
   * @param token 임시 토큰
   */
  private void scheduleTokenBlacklist(String token) {
    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        blacklistTokenService.addToBlacklist(token);
      }
    }, MemberService.TEMP_TOKEN_EXPIRATION_TIME * 1000L);
  }

  /**
   * 회원 탈퇴 : 관련 DB 전체 삭제
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   */
  @Transactional
  public void deleteMember(UserDetails userDetails, Long memberId) {
    MemberEntity member = memberRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

    if (!member.getId().equals(memberId)) {
      throw new CustomException(NO_AUTHORITY);
    }

    s3ImageUpload.deleteAllImages(memberId);

    memberRepository.delete(member);
  }
}
