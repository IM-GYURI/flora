package plannery.flora.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import plannery.flora.dto.flora.FloraDto;
import plannery.flora.service.FloraService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members/{memberId}/flora")
public class FloraController {

  private final FloraService floraService;

  /**
   * 플로라 조회
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @return FloraDto : 카운트, 플로라 타입
   */
  @GetMapping
  public ResponseEntity<FloraDto> getFlora(@AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long memberId) {
    return ResponseEntity.ok(floraService.getFlora(userDetails, memberId));
  }
}
