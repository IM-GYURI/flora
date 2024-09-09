package plannery.flora.service;

import static plannery.flora.exception.ErrorCode.BLACKLIST_TOKEN_ADD_FAILED;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import plannery.flora.exception.CustomException;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistTokenService {

  private final RedisTemplate<String, String> redisStringTemplate;

  /**
   * 블랙리스트에 토큰 추가
   *
   * @param token 토큰 정보
   */
  public void addToBlacklist(String token) {
    if (token.startsWith("Bearer ")) {
      token = token.substring(7);
    }
    log.info("블랙리스트에 토큰 추가 : " + token);

    try {
      redisStringTemplate.opsForValue().set(token, "logout", 60, TimeUnit.MINUTES);
    } catch (Exception e) {
      throw new CustomException(BLACKLIST_TOKEN_ADD_FAILED);
    }
  }

  /**
   * 블랙리스트에 토큰이 존재하는지 여부 확인
   *
   * @param token 토큰 정보
   * @return 토큰이 블랙리스트에 존재하면 true, 존재하지 않으면 false
   */
  public boolean isTokenBlacklist(String token) {
    return redisStringTemplate.opsForValue().get(token) != null;
  }
}
