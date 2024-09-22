package plannery.flora.service;

import static plannery.flora.exception.ErrorCode.MEMBER_NOT_FOUND;
import static plannery.flora.exception.ErrorCode.NO_AUTHORITY;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import plannery.flora.component.SecurityUtils;
import plannery.flora.dto.notification.NotificationCreateDto;
import plannery.flora.dto.notification.NotificationListDto;
import plannery.flora.entity.MemberEntity;
import plannery.flora.entity.NotificationEntity;
import plannery.flora.entity.NotificationListEntity;
import plannery.flora.enums.UserRole;
import plannery.flora.exception.CustomException;
import plannery.flora.repository.MemberRepository;
import plannery.flora.repository.NotificationListRepository;
import plannery.flora.repository.NotificationRepository;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final MemberRepository memberRepository;
  private final NotificationRepository notificationRepository;
  private final NotificationListRepository notificationListRepository;
  private final SecurityUtils securityUtils;

  private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

  @Value("${jwt.secret.expiration}")
  private long sseValidTime;

  /**
   * SSE 구독
   *
   * @param email       회원 이메일
   * @param lastEventId 마지막 이벤트ID
   * @return SseEmitter 객체
   */
  public SseEmitter subscribe(String email, String lastEventId) {
    MemberEntity member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

    SseEmitter emitter = new SseEmitter(sseValidTime);
    emitters.computeIfAbsent(member.getId(), k -> new ArrayList<>()).add(emitter);

    emitter.onCompletion(() -> {
      List<SseEmitter> emitterList = emitters.get(member.getId());
      if (emitterList != null) {
        emitterList.remove(emitter);
        if (emitterList.isEmpty()) {
          emitters.remove(member.getId());
        }
      }
    });

    emitter.onTimeout(emitter::complete);

    try {
      emitter.send("Connected to SSE");
    } catch (IOException e) {
      emitter.completeWithError(e);
    }

    if (lastEventId != null && !lastEventId.isEmpty()) {
      List<NotificationEntity> newNotifications = notificationRepository.findAllByEventIdAfter(
          lastEventId);
      for (NotificationEntity notification : newNotifications) {
        try {
          emitter.send(notification.getMessage() + ";" + notification.getEventId());
        } catch (IOException e) {
          emitter.completeWithError(e);
        }
      }
    }

    return emitter;
  }

  /**
   * SSE 연결 해지
   *
   * @param memberId 회원ID
   */
  public void removeEmitter(Long memberId) {
    List<SseEmitter> emitterList = emitters.get(memberId);

    if (emitterList != null) {
      emitterList.forEach(SseEmitter::complete);
      emitters.remove(memberId);
    }
  }

  /**
   * 알림 생성 : NotificationEntity 생성 -> 회원별 NotificationListEntity 생성 및 SSE 알림 전송
   *
   * @param notificationCreateDto : 제목, 내용
   */
  @Transactional
  public void sendNotificationToAllMembers(UserDetails userDetails,
      NotificationCreateDto notificationCreateDto) {
    MemberEntity memberEntity = memberRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

    if (!memberEntity.getRole().equals(UserRole.ROLE_ADMIN)) {
      throw new CustomException(NO_AUTHORITY);
    }

    String eventId = UUID.randomUUID().toString();

    NotificationEntity notification = NotificationEntity.builder()
        .message(notificationCreateDto.getMessage())
        .eventId(eventId)
        .build();

    notificationRepository.save(notification);

    List<MemberEntity> allMembers = memberRepository.findAll().stream()
        .filter(member -> !member.getRole().equals(UserRole.ROLE_ADMIN))
        .toList();

    allMembers.forEach(member -> {
      NotificationListEntity notificationList = NotificationListEntity.builder()
          .member(member)
          .notification(notification)
          .isRead(false)
          .build();
      notificationListRepository.save(notificationList);

      emitters.get(member.getId()).forEach(emitter -> {
        try {
          emitter.send(notification.getMessage() + ";" + eventId);
        } catch (IOException e) {
          emitter.completeWithError(e);
        }
      });
    });
  }

  /**
   * 알림 목록 조회
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @return List<NotificationListDto> : 메세지, 연월일, 읽음 여부
   */
  public List<NotificationListDto> getNotifications(UserDetails userDetails,
      Long memberId) {
    securityUtils.validateUserDetails(userDetails, memberId);

    List<NotificationListEntity> notificationList = notificationListRepository.findAllByMemberId(
        memberId);

    List<NotificationListDto> notificationListDtos = notificationList.stream()
        .map(notification -> NotificationListDto.builder()
            .message(notification.getNotification().getMessage())
            .date(notification.getNotification().getCreatedAt().toLocalDate())
            .isRead(notification.isRead())
            .build())
        .toList();

    markNotificationsAsRead(notificationList, memberId);

    return notificationListDtos;
  }

  /**
   * 알림 읽음 처리
   *
   * @param notificationList List<NotificationListEntity>
   * @param memberId         회원ID
   */
  @Async
  public void markNotificationsAsRead(List<NotificationListEntity> notificationList,
      Long memberId) {
    notificationList.forEach(notification -> {
      if (notification.getMember().getId().equals(memberId) && !notification.isRead()) {
        notification.updateRead(true);
      }
    });

    notificationListRepository.saveAll(notificationList);
  }
}
