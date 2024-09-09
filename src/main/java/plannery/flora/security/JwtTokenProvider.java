package plannery.flora.security;

import static plannery.flora.exception.ErrorCode.INVALID_MEMBER_ID;
import static plannery.flora.exception.ErrorCode.MEMBER_NOT_FOUND;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import plannery.flora.entity.MemberEntity;
import plannery.flora.enums.UserRole;
import plannery.flora.exception.CustomException;
import plannery.flora.repository.MemberRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

  private static final String KEY_MEMBER_ID = "memberId";
  private static final String KEY_EMAIL = "email";
  private static final String KEY_ROLE = "role";


  @Value("${jwt.secret.key}")
  private String secretKeyString;

  @Value("${jwt.secret.expiration}")
  private long tokenValidTime;

  private final MemberRepository memberRepository;
  private Key secretKey;

  @PostConstruct
  public void init() {
    byte[] keyBytes = Base64.getDecoder().decode(secretKeyString);
    secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
  }

  /**
   * JWT 토큰 생성
   *
   * @param userId 회원 ID
   * @param email  회원 이메일
   * @return 생성된 JWT 토큰
   */
  public String generateToken(Long userId, String email, UserRole role) {
    Claims claims = Jwts.claims();
    claims.put(KEY_MEMBER_ID, userId);
    claims.put(KEY_EMAIL, email);
    claims.put(KEY_ROLE, role.name());

    Date now = new Date();
    Date expiredTime = new Date(now.getTime() + tokenValidTime);

    return Jwts.builder()
        .setClaims(claims)
        .setIssuedAt(now)
        .setExpiration(expiredTime)
        .signWith(secretKey, SignatureAlgorithm.HS256)
        .compact();
  }

  /**
   * JWT 토큰 유효성 검사
   *
   * @param token 검사할 JWT 토큰
   * @return 토큰의 유효성 여부
   */
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder()
          .setSigningKey(secretKey)
          .build()
          .parseClaimsJws(token);

      return true;
    } catch (ExpiredJwtException e) {
      log.error("만료된 JWT 토큰", e);
    } catch (Exception e) {
      log.error("JWT 토큰 유효성 검사 실패", e);
    }

    return false;
  }

  /**
   * JWT 토큰에서 인증 정보 추출
   *
   * @param token JWT 토큰
   * @return 인증 정보
   */
  public Authentication getAuthentication(String token) {
    Claims claims = Jwts.parserBuilder()
        .setSigningKey(secretKey)
        .build()
        .parseClaimsJws(token)
        .getBody();

    Long userId = claims.get(KEY_MEMBER_ID, Long.class);
    String email = claims.get(KEY_EMAIL, String.class);
    UserRole role = UserRole.valueOf(claims.get(KEY_ROLE, String.class));

    UserDetails userDetails = getUserDetails(userId, email);

    return new UsernamePasswordAuthenticationToken(userDetails, token,
        userDetails.getAuthorities());
  }

  private UserDetails getUserDetails(Long userId, String email) {
    MemberEntity member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

    if (!member.getId().equals(userId)) {
      throw new CustomException(INVALID_MEMBER_ID);
    }

    return buildUserDetails(member.getEmail(), member.getPassword(), member.getRole());
  }

  private UserDetails buildUserDetails(String email, String password, UserRole role) {
    return User.builder()
        .username(email)
        .password(password)
        .authorities(Collections.singletonList(new SimpleGrantedAuthority(role.name())))
        .build();
  }
}
