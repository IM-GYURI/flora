package plannery.flora.controller;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import plannery.flora.service.OAuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth")
public class OAuthController {

  private final OAuthService oAuthService;

  /**
   * 네이버 소셜 로그인 or 회원가입
   *
   * @param request 네이버 OAuth 액세스 토큰
   * @return JWT 토큰 반환
   */
  @PostMapping("/naver")
  public ResponseEntity<?> naverLogin(@RequestBody Map<String, String> request) {
    String accessToken = request.get("accessToken");
    String jwtToken = oAuthService.naverLoginOrSignUpWithToken(accessToken);
    return ResponseEntity.ok(Map.of("token", jwtToken));
  }

  /**
   * 카카오 소셜 로그인 or 회원 가입
   *
   * @param request 카카오 OAuth 액세스 토큰
   * @return JWT 토큰 반환
   */
  @PostMapping("/kakao")
  public ResponseEntity<?> kakaoLogin(@RequestBody Map<String, String> request) {
    String accessToken = request.get("accessToken");
    String jwtToken = oAuthService.kakaoLoginOrSignUpWithToken(accessToken);
    return ResponseEntity.ok(Map.of("token", jwtToken));
  }

  /**
   * 구글 소셜 로그인 or 회원가입
   *
   * @param request 구글 OAuth 액세스 토큰
   * @return JWT 토큰 반환
   */
  @PostMapping("/google")
  public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> request) {
    String accessToken = request.get("accessToken");
    String jwtToken = oAuthService.googleLoginOrSignUpWithToken(accessToken);
    return ResponseEntity.ok(Map.of("token", jwtToken));
  }
}
