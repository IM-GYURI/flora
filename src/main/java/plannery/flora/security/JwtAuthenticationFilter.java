package plannery.flora.security;

import static plannery.flora.exception.ErrorCode.INVALID_TOKEN;
import static plannery.flora.exception.ErrorCode.TOKEN_BLACKLISTED;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import plannery.flora.exception.CustomException;
import plannery.flora.exception.ErrorCode;
import plannery.flora.service.BlacklistTokenService;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String TOKEN_HEADER = "Authorization";
  private static final String TOKEN_PREFIX = "Bearer ";

  private final JwtTokenProvider jwtTokenProvider;
  private final BlacklistTokenService blacklistTokenService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String token = resolveToken(request);

    if (StringUtils.hasText(token)) {
      try {
        if (jwtTokenProvider.validateToken(token)) {
          if (!blacklistTokenService.isTokenBlacklist(token)) {
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
          } else {
            throw new CustomException(TOKEN_BLACKLISTED);
          }
        } else {
          throw new CustomException(INVALID_TOKEN);
        }
      } catch (CustomException e) {
        setErrorResponse(response, e.getErrorCode());
        return;
      } catch (Exception e) {
        throw e;
      }
    }

    filterChain.doFilter(request, response);
  }

  private String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader(TOKEN_HEADER);

    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
      return bearerToken.substring(TOKEN_PREFIX.length());
    }

    return null;
  }

  private void setErrorResponse(HttpServletResponse response, ErrorCode errorCode)
      throws IOException {
    response.setContentType("application/json;charset=UTF-8");
    response.setStatus(errorCode.getStatus());
    response.getWriter().write(errorCode.getMessage());
  }
}
