package plannery.flora.service;

import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import plannery.flora.dto.member.OAuthUserDto;
import plannery.flora.entity.MemberEntity;
import plannery.flora.enums.UserRole;
import plannery.flora.repository.MemberRepository;
import plannery.flora.security.JwtTokenProvider;
import plannery.flora.util.RandomGenerator;

@Service
@RequiredArgsConstructor
public class OAuthService {

  private final RestTemplate restTemplate;
  private final MemberRepository memberRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final PasswordEncoder passwordEncoder;
  private final ImageService imageService;
  private final FloraService floraService;

  /**
   * 네이버 소셜 로그인 or 회원가입 처리
   *
   * @param accessToken 네이버 OAuth 액세스 토큰
   * @return JWT 토큰 반환
   */
  public String naverLoginOrSignUpWithToken(String accessToken) {
    OAuthUserDto oAuthUserDto = fetchUserInfoFromNaver(accessToken);
    return loginOrSignUp(oAuthUserDto);
  }

  /**
   * 카카오 소셜 로그인 or 회원가입 처리
   *
   * @param accessToken 카카오 OAuth 액세스 토큰
   * @return JWT 토큰 반환
   */
  public String kakaoLoginOrSignUpWithToken(String accessToken) {
    OAuthUserDto oAuthUserDto = fetchUserInfoFromKakao(accessToken);
    return loginOrSignUp(oAuthUserDto);
  }

  /**
   * 구글 소셜 로그인 or 회원가입 처리
   *
   * @param accessToken 구글 OAuth 액세스 토큰
   * @return JWT 토큰 반환
   */
  public String googleLoginOrSignUpWithToken(String accessToken) {
    OAuthUserDto oAuthUserDto = fetchUserInfoFromGoogle(accessToken);
    return loginOrSignUp(oAuthUserDto);
  }

  /**
   * 네이버로부터 사용자 정보 가져오기
   *
   * @param accessToken OAuth 액세스 토큰
   * @return OAuthUserDto 사용자 정보
   */
  private OAuthUserDto fetchUserInfoFromNaver(String accessToken) {
    String url = "https://openapi.naver.com/v1/nid/me";
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + accessToken);

    HttpEntity<String> entity = new HttpEntity<>(headers);
    ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

    Map<String, Object> userInfo = (Map<String, Object>) response.getBody().get("response");
    String email = (String) userInfo.get("email");

    return new OAuthUserDto(email);
  }

  /**
   * 카카오로부터 사용자 정보 가져오기
   *
   * @param accessToken OAuth 액세스 토큰
   * @return OAuthUserDto 사용자 정보
   */
  private OAuthUserDto fetchUserInfoFromKakao(String accessToken) {
    String url = "https://kapi.kakao.com/v2/user/me";
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + accessToken);

    HttpEntity<String> entity = new HttpEntity<>(headers);
    ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

    Map<String, Object> userInfo = (Map<String, Object>) response.getBody();
    Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
    String email = (String) kakaoAccount.get("email");

    return new OAuthUserDto(email);
  }

  /**
   * 구글로부터 사용자 정보 가져오기
   *
   * @param accessToken OAuth 액세스 토큰
   * @return OAuthUserDto 사용자 정보
   */
  private OAuthUserDto fetchUserInfoFromGoogle(String accessToken) {
    String url = "https://www.googleapis.com/oauth2/v3/userinfo";
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + accessToken);

    HttpEntity<String> entity = new HttpEntity<>(headers);
    ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

    Map<String, Object> userInfo = response.getBody();
    String email = (String) userInfo.get("email");

    return new OAuthUserDto(email);
  }

  /**
   * 소셜 로그인 or 회원가입 처리
   *
   * @param oAuthUserDto OAuth 사용자 정보
   * @return JWT 토큰 반환
   */
  private String loginOrSignUp(OAuthUserDto oAuthUserDto) {
    String email = oAuthUserDto.getEmail();

    Optional<MemberEntity> existingMember = memberRepository.findByEmail(email);

    if (existingMember.isPresent()) {
      return handleExistingMember(existingMember.get());
    } else {
      return handleNewMember(email);
    }
  }

  /**
   * 기존 회원 처리
   *
   * @param member 기존 회원 엔티티
   * @return JWT 토큰 반환
   */
  private String handleExistingMember(MemberEntity member) {
    floraService.updateFloraOnLogin(member.getId());
    return jwtTokenProvider.generateToken(member.getId(), member.getEmail(), member.getRole());
  }

  /**
   * 신규 회원 처리
   *
   * @param email 신규 회원 이메일
   * @return JWT 토큰 반환
   */
  private String handleNewMember(String email) {
    MemberEntity newMember = MemberEntity.builder()
        .email(email)
        .password(
            passwordEncoder.encode(RandomGenerator.generateTemporaryPassword()))
        .role(UserRole.ROLE_MEMBER)
        .build();

    memberRepository.save(newMember);
    imageService.createDefaultImage(newMember.getId());
    floraService.createMyFlora(newMember.getId());

    return jwtTokenProvider.generateToken(newMember.getId(), newMember.getEmail(),
        newMember.getRole());
  }
}