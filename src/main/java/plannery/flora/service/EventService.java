package plannery.flora.service;

import static plannery.flora.exception.ErrorCode.EVENT_NOT_FOUND;
import static plannery.flora.exception.ErrorCode.INVALID_DATETIME;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plannery.flora.component.SecurityUtils;
import plannery.flora.dto.event.DDayDto;
import plannery.flora.dto.event.EventCreateDto;
import plannery.flora.dto.event.EventListByDateDto;
import plannery.flora.dto.event.EventListDto;
import plannery.flora.entity.EventEntity;
import plannery.flora.entity.MemberEntity;
import plannery.flora.exception.CustomException;
import plannery.flora.repository.EventRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

  private final SecurityUtils securityUtils;
  private final EventRepository eventRepository;

  /**
   * 이벤트 생성 : 종료일시는 시작일시보다 앞설 수 없음
   *
   * @param userDetails    사용자 정보
   * @param memberId       회원ID
   * @param eventCreateDto : 제목, 설명, 시작일시, 종료일시, 인덱스, 디데이 설정 여부, 하루종일 설정 여부
   */
  public void createEvent(UserDetails userDetails, Long memberId, EventCreateDto eventCreateDto) {
    MemberEntity member = securityUtils.validateUserDetails(userDetails, memberId);

    if (eventCreateDto.getStartDateTime().isAfter(eventCreateDto.getEndDateTime())) {
      throw new CustomException(INVALID_DATETIME);
    }

    EventEntity event = EventEntity.builder()
        .title(eventCreateDto.getTitle())
        .description(eventCreateDto.getDescription())
        .startDateTime(eventCreateDto.getStartDateTime())
        .endDateTime(eventCreateDto.getEndDateTime())
        .indexColor(eventCreateDto.getIndexColor())
        .isDDay(eventCreateDto.isDDay())
        .isAllDay(eventCreateDto.isAllDay())
        .member((member))
        .build();

    eventRepository.save(event);
  }

  /**
   * 이벤트 개별 조회
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @param eventId     이벤트ID
   * @return EventCreateDto : 제목, 설명, 시작일시, 종료일시, 인덱스, 디데이 설정 여부, 하루종일 설정 여부
   */
  public EventCreateDto getEvent(UserDetails userDetails, Long memberId, Long eventId) {
    securityUtils.validateUserDetails(userDetails, memberId);

    EventEntity event = eventRepository.findById(eventId)
        .orElseThrow(() -> new CustomException(EVENT_NOT_FOUND));

    return EventCreateDto.builder()
        .title(event.getTitle())
        .description(event.getDescription())
        .startDateTime(event.getStartDateTime())
        .endDateTime(event.getEndDateTime())
        .indexColor(event.getIndexColor())
        .isDDay(event.isDDay())
        .isAllDay(event.isAllDay())
        .build();
  }

  /**
   * 오늘의 이벤트 전체 조회
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @param date        오늘 날짜
   * @return List<EventListDto> : 이벤트ID, 제목, 시작시간, 종료시간, 인덱스, 하루종일 설정 여부
   */
  public List<EventListByDateDto> getEventsByDate(UserDetails userDetails, Long memberId,
      LocalDate date) {
    securityUtils.validateUserDetails(userDetails, memberId);

    LocalDateTime startOfDay = date.atStartOfDay();
    LocalDateTime endOfDay = date.atTime(23, 59, 59);

    List<EventEntity> eventList = eventRepository.findAllByMemberIdAndStartDateTimeLessThanEqualAndEndDateTimeGreaterThanEqualOrStartDateTimeBetweenOrEndDateTimeBetween(
        memberId, endOfDay, startOfDay, startOfDay, endOfDay, startOfDay, endOfDay);

    return eventList.stream()
        .map(event -> EventListByDateDto.builder()
            .eventId(event.getId())
            .title(event.getTitle())
            .startTime(event.getStartDateTime().toLocalTime())
            .endTime(event.getEndDateTime().toLocalTime())
            .isAllDay(event.isAllDay())
            .indexColor(event.getIndexColor()).build())
        .toList();
  }

  /**
   * 월별 이벤트 조회
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @param yearMonth   연월 e.g. "2024-09"
   * @return List<EventListByMonthDto> : 이벤트ID, 제목, 시작날짜, 종료날짜, 인덱스, 하루종일 설정 여부
   */
  public List<EventListDto> getEventsByMonth(UserDetails userDetails, Long memberId,
      String yearMonth) {
    securityUtils.validateUserDetails(userDetails, memberId);

    YearMonth parsedYearMonth = YearMonth.parse(yearMonth, DateTimeFormatter.ofPattern("yyyy-MM"));
    LocalDate startDate = parsedYearMonth.atDay(1);
    LocalDate endDate = parsedYearMonth.atEndOfMonth().plusDays(1);

    LocalDateTime startDateTime = startDate.atStartOfDay();
    LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

    List<EventEntity> eventList = eventRepository.findEventsForMemberWithinMonth(memberId,
        startDateTime, endDateTime);

    return eventList.stream()
        .map(event -> EventListDto.builder()
            .eventId(event.getId())
            .title(event.getTitle())
            .startDateTime(event.getStartDateTime())
            .endDateTime(event.getEndDateTime())
            .indexColor(event.getIndexColor())
            .isAllDay(event.isAllDay())
            .build())
        .toList();
  }

  /**
   * 이벤트 전체 조회
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @return List<EventListDto> : 이벤트ID, 제목, 시작일시, 종료일시, 인덱스, 하루종일 설정 여부
   */
  public List<EventListDto> getAllEvent(UserDetails userDetails, Long memberId) {
    securityUtils.validateUserDetails(userDetails, memberId);

    List<EventEntity> eventList = eventRepository.findAllByMemberId(memberId);

    return eventList.stream()
        .map(event -> EventListDto.builder()
            .eventId(event.getId())
            .title(event.getTitle())
            .startDateTime(event.getStartDateTime())
            .endDateTime(event.getEndDateTime())
            .indexColor(event.getIndexColor())
            .isAllDay(event.isAllDay())
            .build())
        .sorted(Comparator.comparing(EventListDto::getStartDateTime))
        .toList();
  }

  /**
   * 디데이 목록 조회
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @return List<DDayDto> : 이벤트ID, 제목, 시작날짜, 남은 날
   */
  public List<DDayDto> getDDayList(UserDetails userDetails, Long memberId) {
    securityUtils.validateUserDetails(userDetails, memberId);

    List<EventEntity> dDayList = eventRepository.findDDayEventsByMemberId(memberId,
        LocalDate.now().atStartOfDay());

    return dDayList.stream()
        .map(dDay -> DDayDto.builder()
            .eventId(dDay.getId())
            .title(dDay.getTitle())
            .startDate(dDay.getStartDateTime().toLocalDate())
            .remain(
                Period.between(LocalDate.now(), dDay.getStartDateTime().toLocalDate()).getDays())
            .build())
        .sorted(Comparator.comparingInt(DDayDto::getRemain))
        .toList();
  }

  /**
   * 이벤트 수정 : 종료일시는 시작일시보다 앞설 수 없음
   *
   * @param userDetails    사용자 정보
   * @param memberId       회원ID
   * @param eventId        이벤트 ID
   * @param eventCreateDto : 제목, 설명, 시작일시, 종료일시, 인덱스, 디데이 설정 여부, 하루종일 설정 여부
   */
  public void updateEvent(UserDetails userDetails, Long memberId, Long eventId,
      EventCreateDto eventCreateDto) {
    securityUtils.validateUserDetails(userDetails, memberId);

    if (eventCreateDto.getStartDateTime().isAfter(eventCreateDto.getEndDateTime())) {
      throw new CustomException(INVALID_DATETIME);
    }

    EventEntity event = eventRepository.findById(eventId)
        .orElseThrow(() -> new CustomException(EVENT_NOT_FOUND));

    event.updateEvent(eventCreateDto.getTitle(), eventCreateDto.getDescription(),
        eventCreateDto.getStartDateTime(), eventCreateDto.getEndDateTime(),
        eventCreateDto.getIndexColor(), eventCreateDto.isDDay(), eventCreateDto.isAllDay());
  }

  /**
   * 이벤트 삭제
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @param eventId     이벤트ID
   */
  public void deleteEvent(UserDetails userDetails, Long memberId, Long eventId) {
    securityUtils.validateUserDetails(userDetails, memberId);

    EventEntity event = eventRepository.findById(eventId)
        .orElseThrow(() -> new CustomException(EVENT_NOT_FOUND));

    eventRepository.delete(event);
  }
}
