package plannery.flora.controller;

import static plannery.flora.enums.ResponseMessage.SUCCESS_NOTIFICATION_CREATE;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import plannery.flora.dto.notification.NotificationCreateDto;
import plannery.flora.dto.notification.NotificationListDto;
import plannery.flora.service.NotificationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

  private final NotificationService notificationService;

  /**
   * 알림 생성 : 관리자
   *
   * @param notificationCreateDto : 제목
   * @return "알림 생성 완료"
   */
  @PostMapping
  public ResponseEntity<String> sendNotificationToAllMembers(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestBody @Valid NotificationCreateDto notificationCreateDto) {
    notificationService.sendNotificationToAllMembers(userDetails, notificationCreateDto);

    return ResponseEntity.ok(SUCCESS_NOTIFICATION_CREATE.getMessage());
  }

  /**
   * SSE 구독
   *
   * @param userDetails 사용자 정보
   * @param lastEventId 마지막 이벤트ID
   * @return SseEmitter 객체
   */
  @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
  public ResponseEntity<SseEmitter> subscribe(@AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {
    return ResponseEntity.ok(notificationService.subscribe(userDetails.getUsername(), lastEventId));
  }

  /**
   * 알림 목록 조회
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @return List<NotificationListDto> : 메세지, 연월일, 읽음 여부
   */
  @GetMapping("/{memberId}")
  public ResponseEntity<List<NotificationListDto>> getNotificationList(
      @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long memberId) {
    return ResponseEntity.ok(notificationService.getNotifications(userDetails, memberId));
  }
}
