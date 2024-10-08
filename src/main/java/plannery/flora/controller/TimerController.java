package plannery.flora.controller;

import static plannery.flora.enums.ResponseMessage.SUCCESS_TIMER_SAVE;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import plannery.flora.dto.timer.TimerCreateDto;
import plannery.flora.dto.timer.TimerListDto;
import plannery.flora.enums.TodoType;
import plannery.flora.service.TimerService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members/{memberId}/timers")
public class TimerController {

  private final TimerService timerService;

  /**
   * 타이머 저장
   *
   * @param userDetails    사용자 정보
   * @param memberId       회원ID
   * @param timerCreateDto : 투두ID, 초단위 시간
   * @return "타이머 저장 완료"
   */
  @PostMapping
  public ResponseEntity<String> saveTimer(@AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long memberId, @RequestBody TimerCreateDto timerCreateDto) {
    timerService.saveTimer(userDetails, memberId, timerCreateDto);

    return ResponseEntity.ok(SUCCESS_TIMER_SAVE.getMessage());
  }

  /**
   * 타이머 목록 조회
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @param isRoutine   루틴 여부
   * @param todoType    투두 타입 : TODO_STUDY, TODO_LIFE
   * @return List<TimerListDto> : todoId, title, timerId, duration
   */
  @GetMapping("/list")
  public ResponseEntity<List<TimerListDto>> getTimers(
      @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long memberId,
      @RequestParam boolean isRoutine, @RequestParam TodoType todoType) {
    return ResponseEntity.ok(timerService.getTimers(userDetails, memberId, isRoutine, todoType));
  }

  /**
   * 오늘의 누적 시간 조회
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @return 누적 시간 (초단위)
   */
  @GetMapping("/total")
  public ResponseEntity<Long> getTotalDuration(@AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long memberId) {
    return ResponseEntity.ok(timerService.getTotalDuration(userDetails, memberId));
  }
}
