package plannery.flora.controller;

import static plannery.flora.enums.ResponseMessage.SUCCESS_EVENT_CREATE;
import static plannery.flora.enums.ResponseMessage.SUCCESS_EVENT_DELETE;
import static plannery.flora.enums.ResponseMessage.SUCCESS_EVENT_UPDATE;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import plannery.flora.dto.event.DDayDto;
import plannery.flora.dto.event.EventCreateDto;
import plannery.flora.dto.event.EventListByDateDto;
import plannery.flora.dto.event.EventListDto;
import plannery.flora.service.EventService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members/{memberId}/events")
public class EventController {

  private final EventService eventService;

  /**
   * 이벤트 생성 : 종료일시는 시작일시보다 앞설 수 없음
   *
   * @param userDetails    사용자 정보
   * @param memberId       회원ID
   * @param eventCreateDto : 제목, 설명, 시작일시, 종료일시, 인덱스, 디데이 설정 여부
   * @return
   */
  @PostMapping
  public ResponseEntity<String> createEvent(@AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long memberId, @RequestBody @Valid EventCreateDto eventCreateDto) {
    eventService.createEvent(userDetails, memberId, eventCreateDto);

    return ResponseEntity.ok(SUCCESS_EVENT_CREATE.getMessage());
  }

  /**
   * 이벤트 개별 조회
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @param eventId     이벤트ID
   * @return EventCreateDto : 제목, 설명, 시작일시, 종료일시, 인덱스, 디데이 설정 여부
   */
  @GetMapping("/{eventId}")
  public ResponseEntity<EventCreateDto> getEvent(@AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long memberId, @PathVariable Long eventId) {
    return ResponseEntity.ok(eventService.getEvent(userDetails, memberId, eventId));
  }

  /**
   * 오늘의 이벤트 전체 조회
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @param date        오늘 날짜
   * @return List<EventListDto> : 이벤트ID, 제목, 시작시간, 종료시간, 인덱스
   */
  @GetMapping("/date")
  public ResponseEntity<List<EventListByDateDto>> getEventsByDate(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long memberId,
      @RequestParam("date") @DateTimeFormat(iso = ISO.DATE) LocalDate date) {
    return ResponseEntity.ok(eventService.getEventsByDate(userDetails, memberId, date));
  }

  /**
   * 월별 이벤트 조회
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @param yearMonth   연월  e.g. "2024-09"
   * @return List<EventListByMonthDto> : 이벤트ID, 제목, 시작날짜, 종료날짜, 인덱스
   */
  @GetMapping("/month")
  public ResponseEntity<List<EventListDto>> getEventsByMonth(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long memberId, @RequestParam("yearMonth") String yearMonth) {
    return ResponseEntity.ok(eventService.getEventsByMonth(userDetails, memberId, yearMonth));
  }

  /**
   * 이벤트 전체 조회
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @return List<EventListDto> : 이벤트ID, 제목, 시작일시, 종료일시, 인덱스, 하루종일 설정 여부
   */
  @GetMapping
  public ResponseEntity<List<EventListDto>> getAllEvent(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long memberId) {
    return ResponseEntity.ok(eventService.getAllEvent(userDetails, memberId));
  }

  /**
   * 디데이 목록 조회
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @return List<DDayDto> : 이벤트ID, 제목, 시작날짜, 남은 날
   */
  @GetMapping("/dday")
  public ResponseEntity<List<DDayDto>> getDDayList(@AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long memberId) {
    return ResponseEntity.ok(eventService.getDDayList(userDetails, memberId));
  }

  /**
   * 이벤트 수정 : 종료일시는 시작일시보다 앞설 수 없음
   *
   * @param userDetails    사용자 정보
   * @param memberId       회원ID
   * @param eventId        이벤트ID
   * @param eventCreateDto : 제목, 설명, 시작일시, 종료일시, 인덱스, 디데이 설졍 여부
   * @return "이벤트 수정 완료"
   */
  @PutMapping("/{eventId}")
  public ResponseEntity<String> updateEvent(@AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long memberId, @PathVariable Long eventId,
      @RequestBody @Valid EventCreateDto eventCreateDto) {
    eventService.updateEvent(userDetails, memberId, eventId, eventCreateDto);

    return ResponseEntity.ok(SUCCESS_EVENT_UPDATE.getMessage());
  }

  /**
   * 이벤트 삭제
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @param eventId     이벤트ID
   * @return "이벤트 삭제 완료"
   */
  @DeleteMapping("/{eventId}")
  public ResponseEntity<String> deleteEvent(@AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long memberId, @PathVariable Long eventId) {
    eventService.deleteEvent(userDetails, memberId, eventId);

    return ResponseEntity.ok(SUCCESS_EVENT_DELETE.getMessage());
  }
}
