package plannery.flora.service;

import static plannery.flora.exception.ErrorCode.TODO_NOT_FOUND;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plannery.flora.component.SecurityUtils;
import plannery.flora.dto.timer.TimerCreateDto;
import plannery.flora.dto.timer.TimerListDto;
import plannery.flora.entity.TimerEntity;
import plannery.flora.entity.TodoEntity;
import plannery.flora.enums.TodoType;
import plannery.flora.exception.CustomException;
import plannery.flora.repository.TimerRepository;
import plannery.flora.repository.TodoRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class TimerService {

  private final TimerRepository timerRepository;
  private final TodoRepository todoRepository;
  private final SecurityUtils securityUtils;

  /**
   * 타이머 저장
   *
   * @param userDetails    사용자 정보
   * @param memberId       회원ID
   * @param timerCreateDto : 투두ID, 초단위 시간
   */
  public void saveTimer(UserDetails userDetails, Long memberId, TimerCreateDto timerCreateDto) {
    securityUtils.validateUserDetails(userDetails, memberId);

    TodoEntity todoEntity = todoRepository.findById(timerCreateDto.getTodoId())
        .orElseThrow(() -> new CustomException(TODO_NOT_FOUND));

    Optional<TimerEntity> timerEntity = timerRepository.findByTodo(todoEntity);

    if (timerEntity.isPresent()) {
      timerEntity.get().updateDuration(timerCreateDto.getDuration());
    } else {
      TimerEntity newTimerEntity = TimerEntity.builder()
          .todo(todoEntity)
          .duration(timerCreateDto.getDuration())
          .build();

      timerRepository.save(newTimerEntity);
    }
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
  public List<TimerListDto> getTimers(UserDetails userDetails, Long memberId, boolean isRoutine,
      TodoType todoType) {
    securityUtils.validateUserDetails(userDetails, memberId);

    List<TodoEntity> todoEntities;

    if (isRoutine) {
      todoEntities = todoRepository.findTodosByCriteria(memberId, todoType, LocalDate.now(), true);
    } else {
      todoEntities = todoRepository.findTodosByCriteria(memberId, todoType, LocalDate.now(), false);
    }

    return todoEntities.stream()
        .map(todo -> {
          TimerEntity timerEntity = timerRepository.findByTodo(todo)
              .orElse(null);

          return TimerListDto.builder()
              .todoId(todo.getId())
              .title(todo.getTitle())
              .timerId(timerEntity != null ? timerEntity.getId() : null)
              .duration(timerEntity != null ? timerEntity.getDuration() : 0)
              .build();
        })
        .toList();
  }

  /**
   * 오늘의 누적 시간 조회
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @return 누적 시간 (초단위)
   */
  public long getTotalDuration(UserDetails userDetails, Long memberId) {
    securityUtils.validateUserDetails(userDetails, memberId);

    List<TodoEntity> todoEntities = todoRepository.findTodosByDate(memberId, LocalDate.now());

    return todoEntities.stream()
        .mapToLong(todo -> {
          TimerEntity timerEntity = timerRepository.findByTodo(todo)
              .orElse(null);
          return timerEntity != null ? timerEntity.getDuration() : 0;
        })
        .sum();
  }
}
