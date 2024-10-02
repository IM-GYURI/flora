package plannery.flora.service;

import static plannery.flora.exception.ErrorCode.INVALID_DATETIME;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plannery.flora.component.SecurityUtils;
import plannery.flora.dto.todo.TodoCreateDto;
import plannery.flora.dto.todo.TodoResponseDto;
import plannery.flora.entity.MemberEntity;
import plannery.flora.entity.TodoEntity;
import plannery.flora.entity.TodoRepeatEntity;
import plannery.flora.enums.TodoType;
import plannery.flora.exception.CustomException;
import plannery.flora.repository.TodoRepeatRepository;
import plannery.flora.repository.TodoRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class TodoService {

  private final TodoRepository todoRepository;
  private final TodoRepeatRepository todoRepeatRepository;
  private final SecurityUtils securityUtils;

  /**
   * 투두 생성
   *
   * @param userDetails   사용자 정보
   * @param memberId      회원ID
   * @param todoCreateDto : 제목, 투두타입(TODO_STUDY, TODO_LIFE), 루틴 여부, 인덱스 색상, 시작날짜, 종료날짜, 설명, 반복 요일
   */
  public void createTodo(UserDetails userDetails, Long memberId, TodoCreateDto todoCreateDto) {
    MemberEntity member = securityUtils.validateUserDetails(userDetails, memberId);

    if (todoCreateDto.getStartDate() != null && todoCreateDto.getStartDate()
        .isAfter(todoCreateDto.getEndDate())) {
      throw new CustomException(INVALID_DATETIME);
    }

    if (!todoCreateDto.isRoutine()) {
      TodoEntity todoEntity = TodoEntity.builder()
          .member(member)
          .title(todoCreateDto.getTitle())
          .description(todoCreateDto.getDescription())
          .todoType(todoCreateDto.getTodoType())
          .todoDate(todoCreateDto.getStartDate())
          .indexColor(todoCreateDto.getIndexColor())
          .isCompleted(false)
          .build();

      todoRepository.save(todoEntity);
    } else {
      TodoRepeatEntity todoRepeatEntity = TodoRepeatEntity.builder()
          .member(member)
          .title(todoCreateDto.getTitle())
          .description(todoCreateDto.getDescription())
          .todoType(todoCreateDto.getTodoType())
          .isRoutine(todoCreateDto.isRoutine())
          .indexColor(todoCreateDto.getIndexColor())
          .startDate(todoCreateDto.getStartDate())
          .endDate(todoCreateDto.getEndDate())
          .repeatDays(todoCreateDto.getRepeatDays())
          .build();

      todoRepeatRepository.save(todoRepeatEntity);

      LocalDate currentDate = todoCreateDto.getStartDate();
      LocalDate endDate = todoCreateDto.getEndDate();
      List<DayOfWeek> repeatDays = todoCreateDto.getRepeatDays();

      while (!currentDate.isAfter(endDate)) {
        if (repeatDays.contains(currentDate.getDayOfWeek())) {
          TodoEntity todoEntity = TodoEntity.builder()
              .member(member)
              .title(todoCreateDto.getTitle())
              .description(todoCreateDto.getDescription())
              .todoType(todoCreateDto.getTodoType())
              .todoDate(currentDate)
              .indexColor(todoCreateDto.getIndexColor())
              .isCompleted(false)
              .todoRepeat(todoRepeatEntity)
              .build();

          todoRepository.save(todoEntity);
        }
        currentDate = currentDate.plusDays(1);
      }
    }
  }

  /**
   * 투두 목록 조회 : todoType, isRoutine, date에 따라
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @param isRoutine   루틴 여부
   * @param todoType    투두타입 (TODO_STUDY, TODO_LIFE)
   * @param date        날짜
   * @return List<TodoResponseDto> : 투두ID, 제목, 완료 여부
   */
  public List<TodoResponseDto> getTodos(UserDetails userDetails, Long memberId, boolean isRoutine,
      TodoType todoType, LocalDate date) {
    securityUtils.validateUserDetails(userDetails, memberId);

    List<TodoEntity> todoEntities;

    if (isRoutine) {
      todoEntities = todoRepository.findTodosByCriteria(memberId, todoType, date, true);
    } else {
      todoEntities = todoRepository.findTodosByCriteria(memberId, todoType, date, false);
    }

    return todoEntities.stream()
        .map(todo -> TodoResponseDto.builder()
            .todoId(todo.getId())
            .title(todo.getTitle())
            .isCompleted(todo.isCompleted())
            .build())
        .toList();
  }

  /**
   * 투두 완료 체크
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @param todoIds     투두ID 목록
   */
  public void completeTodos(UserDetails userDetails, Long memberId, List<Long> todoIds) {
    securityUtils.validateUserDetails(userDetails, memberId);

    List<TodoEntity> todos = todoRepository.findAllById(todoIds);

    for (TodoEntity todo : todos) {
      todo.completeCheck(!todo.isCompleted());
    }

    todoRepository.saveAll(todos);
  }

//  /**
//   * 투두 개별 조회
//   *
//   * @param userDetails 사용자 정보
//   * @param memberId    회원ID
//   * @param todoId      투두ID
//   * @return todoCreateDto : 제목, 투두타입(TODO_STUDY, TODO_LIFE), 루틴 여부, 인덱스 색상, 시작날짜, 종료날짜, 설명, 반복 요일
//   */
//  public TodoCreateDto getTodo(UserDetails userDetails, Long memberId, Long todoId) {
//    securityUtils.validateUserDetails(userDetails, memberId);
//
//    TodoEntity todoEntity = todoRepository.findById(todoId)
//        .orElseThrow(() -> new CustomException(TODO_NOT_FOUND));
//
//    if (todoEntity.getTodoRepeat() != null) {
//      TodoRepeatEntity todoRepeatEntity = todoRepeatRepository.findById(
//              todoEntity.getTodoRepeat().getId())
//          .orElseThrow(() -> new CustomException(TODO_REPEAT_NOT_FOUND));
//
//      return TodoCreateDto.builder()
//          .title(todoEntity.getTitle())
//          .todoType(todoEntity.getTodoType())
//          .isRoutine(todoRepeatEntity.isRoutine())
//          .indexColor(todoEntity.getIndexColor())
//          .startDate(todoRepeatEntity.getStartDate())
//          .endDate(todoRepeatEntity.getEndDate())
//          .description(todoEntity.getDescription())
//          .repeatDays(todoRepeatEntity.getRepeatDays())
//          .build();
//    } else {
//      return TodoCreateDto.builder()
//          .title(todoEntity.getTitle())
//          .todoType(todoEntity.getTodoType())
//          .isRoutine(false)
//          .indexColor(todoEntity.getIndexColor())
//          .startDate(todoEntity.getTodoDate())
//          .endDate(todoEntity.getTodoDate())
//          .description(todoEntity.getDescription())
//          .repeatDays(Collections.emptyList())
//          .build();
//    }
//  }

//  /**
//   * 투두 수정
//   *
//   * @param userDetails   사용자 정보
//   * @param memberId      회원ID
//   * @param todoId        투두ID
//   * @param todoCreateDto : 제목, 투두타입(TODO_STUDY, TODO_LIFE), 루틴 여부, 인덱스 색상, 시작날짜, 종료날짜, 설명, 반복 요일
//   */
//  public void changeTodo(UserDetails userDetails, Long memberId, Long todoId,
//      TodoCreateDto todoCreateDto) {
//    MemberEntity member = securityUtils.validateUserDetails(userDetails, memberId);
//
//    if (todoCreateDto.getStartDate() != null && todoCreateDto.getStartDate()
//        .isAfter(todoCreateDto.getEndDate())) {
//      throw new CustomException(INVALID_DATETIME);
//    }
//
//    TodoEntity todoEntity = todoRepository.findById(todoId)
//        .orElseThrow(() -> new CustomException(TODO_NOT_FOUND));
//
//    boolean isCurrentlyRoutine = todoEntity.getTodoRepeat() != null;
//
//    if (todoCreateDto.isRoutine()) {
//      if (!isCurrentlyRoutine) {
//        // 비루틴 -> 루틴
//        TodoRepeatEntity newTodoRepeatEntity = TodoRepeatEntity.builder()
//            .member(todoEntity.getMember())
//            .title(todoCreateDto.getTitle())
//            .description(todoCreateDto.getDescription())
//            .todoType(todoCreateDto.getTodoType())
//            .isRoutine(todoCreateDto.isRoutine())
//            .indexColor(todoCreateDto.getIndexColor())
//            .startDate(todoCreateDto.getStartDate())
//            .endDate(todoCreateDto.getEndDate())
//            .repeatDays(todoCreateDto.getRepeatDays())
//            .build();
//        todoRepeatRepository.save(newTodoRepeatEntity);
//
//        todoRepository.deleteById(todoId);
//
//        LocalDate currentDate = todoCreateDto.getStartDate();
//        LocalDate endDate = todoCreateDto.getEndDate();
//        List<DayOfWeek> repeatDays = todoCreateDto.getRepeatDays();
//
//        while (!currentDate.isAfter(endDate)) {
//          if (repeatDays.contains(currentDate.getDayOfWeek())) {
//            TodoEntity newTodoEntity = TodoEntity.builder()
//                .member(member)
//                .title(todoCreateDto.getTitle())
//                .description(todoCreateDto.getDescription())
//                .todoType(todoCreateDto.getTodoType())
//                .todoDate(currentDate)
//                .indexColor(todoCreateDto.getIndexColor())
//                .isCompleted(false)
//                .todoRepeat(newTodoRepeatEntity)
//                .build();
//
//            todoRepository.save(newTodoEntity);
//          }
//          currentDate = currentDate.plusDays(1);
//        }
//      } else {
//        // 루틴 -> 루틴
//        TodoRepeatEntity todoRepeatEntity = todoEntity.getTodoRepeat();
//
//        todoRepeatEntity.updateTodoRepeat(todoCreateDto.getTitle(), todoCreateDto.getDescription(),
//            todoCreateDto.getTodoType(), todoCreateDto.getIndexColor(),
//            todoCreateDto.getStartDate(), todoCreateDto.getEndDate(),
//            todoCreateDto.getRepeatDays());
//
//        todoRepeatRepository.save(todoRepeatEntity);
//
//        List<TodoEntity> existingTodos = todoRepository.findAllByTodoRepeat(todoRepeatEntity);
//        todoRepository.deleteAll(existingTodos);
//
//        LocalDate currentDate = todoCreateDto.getStartDate();
//        LocalDate endDate = todoCreateDto.getEndDate();
//        List<DayOfWeek> repeatDays = todoCreateDto.getRepeatDays();
//
//        while (!currentDate.isAfter(endDate)) {
//          if (repeatDays.contains(currentDate.getDayOfWeek())) {
//            TodoEntity newTodoEntity = TodoEntity.builder()
//                .member(todoEntity.getMember())
//                .title(todoCreateDto.getTitle())
//                .description(todoCreateDto.getDescription())
//                .todoType(todoCreateDto.getTodoType())
//                .todoDate(currentDate)
//                .indexColor(todoCreateDto.getIndexColor())
//                .isCompleted(false) // 기본값 설정
//                .todoRepeat(todoRepeatEntity)
//                .build();
//
//            todoRepository.save(newTodoEntity);
//          }
//          currentDate = currentDate.plusDays(1);
//        }
//      }
//    }
//  }
//
//  /**
//   * 투두 삭제
//   *
//   * @param userDetails 사용자 정보
//   * @param memberId    회원ID
//   * @param todoId      투두ID
//   */
//  public void deleteTodo(UserDetails userDetails, Long memberId, Long todoId) {
//    securityUtils.validateUserDetails(userDetails, memberId);
//
//    todoRepository.deleteById(todoId);
//    todoRepeatRepository.deleteAllByTodoId(todoId);
//  }
}
