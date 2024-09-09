package plannery.flora.controller;

import static plannery.flora.enums.ResponseMessage.SUCCESS_SIGNUP;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
   * @return JWT 토큰이 포함된 응답
   */
  @PostMapping("/signup")
  public ResponseEntity<?> signUp(@RequestBody @Validated SignUpDto signUpDto) {
    String token = memberService.signUpOrSignIn(signUpDto.getEmail(), signUpDto.getPassword());

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);

    return ResponseEntity.ok()
        .headers(httpHeaders)
        .body(SUCCESS_SIGNUP.getMessage());
  }
}
