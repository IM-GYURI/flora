package plannery.flora.controller;

import static plannery.flora.enums.ResponseMessage.SUCCESS_SIGNOUT;
import static plannery.flora.enums.ResponseMessage.SUCCESS_SIGNUP;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import plannery.flora.dto.member.SignUpDto;
import plannery.flora.service.MemberService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

  private final MemberService memberService;

  /**
   * 회원가입 or 로그인 후 JWT 토큰 발급
   *
   * @param signUpDto 회원가입 or 로그인 정보
   * @return JWT 토큰 & "회원가입 완료"
   */
  @PostMapping("/signup")
  public ResponseEntity<String> signUp(@RequestBody @Validated SignUpDto signUpDto) {
    String token = memberService.signUpOrSignIn(signUpDto.getEmail(), signUpDto.getPassword());

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
  @PostMapping("/signout")
  public ResponseEntity<String> signOut(@RequestHeader("Authorization") String token) {
    memberService.signOut(token);

    return ResponseEntity.ok(SUCCESS_SIGNOUT.getMessage());
  }
}
