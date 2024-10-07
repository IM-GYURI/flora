package plannery.flora.controller;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static plannery.flora.enums.ResponseMessage.SUCCESS_MEMBER_DELETE;
import static plannery.flora.enums.ResponseMessage.SUCCESS_PASSWORD_CHANGE;
import static plannery.flora.enums.ResponseMessage.SUCCESS_REFRESH;
import static plannery.flora.enums.ResponseMessage.SUCCESS_SEND_PASSWORD_CHANGE;
import static plannery.flora.enums.ResponseMessage.SUCCESS_SIGNOUT;
import static plannery.flora.enums.ResponseMessage.SUCCESS_SIGNUP;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import plannery.flora.dto.member.MemberInfoDto;
import plannery.flora.dto.member.MemberTokenInfoDto;
import plannery.flora.dto.member.PasswordChangeDto;
import plannery.flora.dto.member.SignUpDto;
import plannery.flora.security.JwtTokenProvider;
import plannery.flora.service.BlacklistTokenService;
import plannery.flora.service.MemberService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

  private final MemberService memberService;
  private final BlacklistTokenService blacklistTokenService;
  private final JwtTokenProvider jwtTokenProvider;

  /**
   * 회원가입 or 로그인 후 JWT 토큰 발급 : 회원용
   *
   * @param signUpDto 회원가입 or 로그인 정보
   * @return JWT 토큰 & "회원가입 완료"
   */
  @PostMapping("/signup")
  public ResponseEntity<String> signUp(@RequestBody @Valid SignUpDto signUpDto) {
    String token = memberService.signUpOrSignIn(signUpDto.getEmail(), signUpDto.getPassword());

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);

    return ResponseEntity.ok()
        .headers(httpHeaders)
        .body(SUCCESS_SIGNUP.getMessage());
  }

  /**
   * 회원가입 or 로그인 후 JWT 토큰 발급 : 관리자용
   *
   * @param signUpDto 회원가입 or 로그인 정보
   * @return JWT 토큰 & "회원가입 완료"
   */
  @PostMapping("/signup/admin")
  public ResponseEntity<String> signUpForAdmin(@RequestBody @Valid SignUpDto signUpDto) {
    String token = memberService.signUpOrSignInForAdmin(signUpDto.getEmail(),
        signUpDto.getPassword());

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);

    return ResponseEntity.ok()
        .headers(httpHeaders)
        .body(SUCCESS_SIGNUP.getMessage());
  }

  /**
   * 로그아웃
   *
   * @param token 토큰 정보
   * @return "로그아웃 성공"
   */
  @PostMapping("/{memberId}/signout")
  public ResponseEntity<String> signOut(@RequestHeader("Authorization") String token,
      @PathVariable Long memberId) {
    memberService.signOut(token, memberId);

    return ResponseEntity.ok(SUCCESS_SIGNOUT.getMessage());
  }

  /**
   * 회원 정보 조회
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @return MemberInfoDto : 이메일, 프로필 이미지 URL
   */
  @GetMapping("/{memberId}")
  public ResponseEntity<MemberInfoDto> getMemberInfo(
      @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long memberId) {
    return ResponseEntity.ok(memberService.getMemberInfo(userDetails, memberId));
  }

  /**
   * 비밀번호 변경
   *
   * @param token             JWT 토큰
   * @param memberId          회원ID
   * @param passwordChangeDto 현재 비밀번호, 새 비밀번호
   * @return "비밀번호 변경 완료"
   */
  @PutMapping("/{memberId}/password")
  public ResponseEntity<String> changePassword(@RequestParam String token,
      @PathVariable Long memberId, @RequestBody @Validated PasswordChangeDto passwordChangeDto) {
    memberService.changePassword(token, memberId, passwordChangeDto);

    return ResponseEntity.ok(SUCCESS_PASSWORD_CHANGE.getMessage());
  }

  /**
   * 비밀번호 찾기 -> 이메일로 임시 비밀번호 발급
   *
   * @param email 이메일
   * @return "임시 비밀번호 전송 완료"
   */
  @PostMapping("/password")
  public ResponseEntity<String> passwordChange(@RequestParam String email) {
    memberService.passwordChange(email);

    return ResponseEntity.ok(SUCCESS_SEND_PASSWORD_CHANGE.getMessage());
  }

  /**
   * 회원 탈퇴 : 관련 DB 전체 삭제
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @return "회원 탈퇴 완료"
   */
  @DeleteMapping("/{memberId}")
  public ResponseEntity<String> deleteMember(@AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long memberId) {
    memberService.deleteMember(userDetails, memberId);

    return ResponseEntity.ok(SUCCESS_MEMBER_DELETE.getMessage());
  }

  /**
   * 토큰 재발급
   *
   * @param authHeader 현재 토큰
   * @return "토큰 재발급 완료"
   */
  @PostMapping("/refresh")
  public ResponseEntity<String> refreshToken(@RequestHeader("Authorization") String authHeader) {
    String token = authHeader.substring("Bearer ".length());

    if (!jwtTokenProvider.validateToken(token)) {
      return ResponseEntity.status(UNAUTHORIZED).build();
    }

    blacklistTokenService.addToBlacklist(token);

    MemberTokenInfoDto memberTokenInfoDto = jwtTokenProvider.getUserInfoFromToken(token);

    String newToken = jwtTokenProvider.generateToken(memberTokenInfoDto.getUserId(),
        memberTokenInfoDto.getEmail(), memberTokenInfoDto.getRole());

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + newToken);

    return ResponseEntity.ok()
        .headers(httpHeaders)
        .body(SUCCESS_REFRESH.getMessage());
  }
}
