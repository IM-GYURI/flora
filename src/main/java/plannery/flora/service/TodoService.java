package plannery.flora.service;

import static plannery.flora.exception.ErrorCode.INVALID_DATETIME;
import static plannery.flora.exception.ErrorCode.TODO_COMPLETED_CHECK_MISS;
import static plannery.flora.exception.ErrorCode.TODO_NOT_FOUND;
import static plannery.flora.exception.ErrorCode.TODO_REPEAT_NOT_FOUND;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plannery.flora.component.SecurityUtils;
import plannery.flora.dto.todo.TodoCheckDto;
import plannery.flora.dto.todo.TodoCreateDto;
import plannery.flora.dto.todo.TodoResponseDto;
import plannery.flora.dto.todo.TodoUpdateDto;
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
            .indexColor(todo.getIndexColor())
            .isCompleted(todo.isCompleted())
            .build())
        .toList();
  }

  /**
   * 투두 완료 체크
   *
   * @param userDetails   사용자 정보
   * @param memberId      회원ID
   * @param todoCheckDtos 투두ID, 완료 여부 목록
   */
  public void completeTodos(UserDetails userDetails, Long memberId,
      List<TodoCheckDto> todoCheckDtos) {
    securityUtils.validateUserDetails(userDetails, memberId);

    for (TodoCheckDto todoCheckDto : todoCheckDtos) {
      TodoEntity todoEntity = todoRepository.findById(todoCheckDto.getTodoId())
          .orElseThrow(() -> new CustomException(TODO_NOT_FOUND));

      if (todoEntity.isCompleted() != todoCheckDto.isCompleted()) {
        throw new CustomException(TODO_COMPLETED_CHECK_MISS);
      }

      todoEntity.completeCheck(!todoCheckDto.isCompleted());
    }
  }

  /**
   * 투두 개별 조회
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @param todoId      투두ID
   * @return todoCreateDto : 제목, 투두타입(TODO_STUDY, TODO_LIFE), 루틴 여부, 인덱스 색상, 시작날짜, 종료날짜, 설명, 반복 요일
   */
  public TodoCreateDto getTodo(UserDetails userDetails, Long memberId, Long todoId) {
    securityUtils.validateUserDetails(userDetails, memberId);

    TodoEntity todoEntity = todoRepository.findById(todoId)
        .orElseThrow(() -> new CustomException(TODO_NOT_FOUND));

    if (todoEntity.getTodoRepeat() != null) {
      TodoRepeatEntity todoRepeatEntity = todoRepeatRepository.findById(
              todoEntity.getTodoRepeat().getId())
          .orElseThrow(() -> new CustomException(TODO_REPEAT_NOT_FOUND));

      return TodoCreateDto.builder()
          .title(todoEntity.getTitle())
          .todoType(todoEntity.getTodoType())
          .isRoutine(todoRepeatEntity.isRoutine())
          .indexColor(todoEntity.getIndexColor())
          .startDate(todoRepeatEntity.getStartDate())
          .endDate(todoRepeatEntity.getEndDate())
          .description(todoEntity.getDescription())
          .repeatDays(todoRepeatEntity.getRepeatDays())
          .build();
    } else {
      return TodoCreateDto.builder()
          .title(todoEntity.getTitle())
          .todoType(todoEntity.getTodoType())
          .isRoutine(false)
          .indexColor(todoEntity.getIndexColor())
          .startDate(todoEntity.getTodoDate())
          .endDate(todoEntity.getTodoDate())
          .description(todoEntity.getDescription())
          .repeatDays(Collections.emptyList())
          .build();
    }
  }

  /**
   * 투두 수정
   *
   * @param userDetails   사용자 정보
   * @param memberId      회원ID
   * @param todoId        투두ID
   * @param todoUpdateDto : 제목, 투두타입(TODO_STUDY, TODO_LIFE), 루틴 여부, 인덱스 색상, 종료날짜, 설명, 반복 요일
   */
  public void changeTodo(UserDetails userDetails, Long memberId, Long todoId,
      TodoUpdateDto todoUpdateDto) {
    MemberEntity member = securityUtils.validateUserDetails(userDetails, memberId);

    TodoEntity todoEntity = todoRepository.findById(todoId)
        .orElseThrow(() -> new CustomException(TODO_NOT_FOUND));

    boolean isCurrentlyRoutine = todoEntity.getTodoRepeat() != null;

    if (todoUpdateDto.isRoutine()) {
      if (!isCurrentlyRoutine) {
        // 비루틴 -> 루틴 : TodoRepeatEntity 생성 -> 현재 TodoEntity에 연결 -> 현재 TodoEntity를 제외한 나머지 TodoEntity 생성
        TodoRepeatEntity newTodoRepeatEntity = TodoRepeatEntity.builder()
            .member(todoEntity.getMember())
            .title(todoUpdateDto.getTitle())
            .description(todoUpdateDto.getDescription())
            .todoType(todoUpdateDto.getTodoType())
            .isRoutine(todoUpdateDto.isRoutine())
            .indexColor(todoUpdateDto.getIndexColor())
            .startDate(todoEntity.getTodoDate())
            .endDate(todoUpdateDto.getEndDate())
            .repeatDays(todoUpdateDto.getRepeatDays())
            .build();
        todoRepeatRepository.save(newTodoRepeatEntity);

        todoEntity.updateTodoRepeat(newTodoRepeatEntity);
        updateTodoEntity(todoEntity, todoUpdateDto);

        createTodosForRepeatDays(todoUpdateDto, member, newTodoRepeatEntity,
            todoEntity.getTodoDate());
      } else {
        // 루틴 -> 루틴 : TodoRepeatEntity 수정 -> 현재 TodoEntity는 내용 수정 -> 현재 TodoEntity의 todoDate 이후의 TodoEntity들은 전체 삭제 후 재생성
        TodoRepeatEntity todoRepeatEntity = todoEntity.getTodoRepeat();

        todoRepeatEntity.updateTodoRepeat(todoUpdateDto.getTitle(), todoUpdateDto.getDescription(),
            todoUpdateDto.getTodoType(), todoUpdateDto.getIndexColor(), todoUpdateDto.getEndDate(),
            todoUpdateDto.getRepeatDays());

        updateTodoEntity(todoEntity, todoUpdateDto);

        deleteFutureTodos(todoRepeatEntity, todoEntity.getTodoDate());
        createTodosForRepeatDays(todoUpdateDto, todoEntity.getMember(), todoRepeatEntity,
            todoEntity.getTodoDate());
      }
    } else {
      if (!isCurrentlyRoutine) {
        // 비루틴 -> 비루틴 : title, todoType, indexColor, description 수정
        updateTodoEntity(todoEntity, todoUpdateDto);
      } else {
        // 루틴 -> 비루틴 : TodoEntity 수정 -> 현재 TodoEntity 외의 TodoEntity들은 전부 삭제
        TodoRepeatEntity todoRepeatEntity = todoEntity.getTodoRepeat();

        updateTodoEntity(todoEntity, todoUpdateDto);
        deleteFutureTodos(todoRepeatEntity, todoEntity.getTodoDate());
      }
    }
  }

  /**
   * TodoEntity 수정 : title, todoType, indexColor, todoDate, description
   *
   * @param todoEntity
   * @param todoUpdateDto
   */
  private void updateTodoEntity(TodoEntity todoEntity, TodoUpdateDto todoUpdateDto) {
    todoEntity.updateTodo(todoUpdateDto.getTitle(), todoUpdateDto.getTodoType(),
        todoUpdateDto.getIndexColor(), todoEntity.getTodoDate(),
        todoUpdateDto.getDescription());
  }

  /**
   * baseDate 이후의 TodoEntity 삭제
   *
   * @param todoRepeatEntity
   * @param baseDate
   */
  private void deleteFutureTodos(TodoRepeatEntity todoRepeatEntity, LocalDate baseDate) {
    List<TodoEntity> existingTodos = todoRepository.findAllByTodoRepeat(todoRepeatEntity);
    List<TodoEntity> todosToDelete = existingTodos.stream()
        .filter(todo -> todo.getTodoDate().isAfter(baseDate))
        .toList();
    todoRepository.deleteAll(todosToDelete);
  }

  /**
   * TodoRepeatEntity에 따른 TodoEntity 생성 : excludedDate 이후
   *
   * @param todoUpdateDto
   * @param member
   * @param todoRepeatEntity
   * @param excludedDate
   */
  private void createTodosForRepeatDays(TodoUpdateDto todoUpdateDto, MemberEntity member,
      TodoRepeatEntity todoRepeatEntity, LocalDate excludedDate) {
    LocalDate currentDate = excludedDate;
    LocalDate endDate = todoUpdateDto.getEndDate();
    List<DayOfWeek> repeatDays = todoUpdateDto.getRepeatDays();

    while (!currentDate.isAfter(endDate)) {
      if (repeatDays.contains(currentDate.getDayOfWeek()) && !currentDate.equals(excludedDate)) {
        TodoEntity newTodoEntity = TodoEntity.builder()
            .member(member)
            .title(todoUpdateDto.getTitle())
            .description(todoUpdateDto.getDescription())
            .todoType(todoUpdateDto.getTodoType())
            .todoDate(currentDate)
            .indexColor(todoUpdateDto.getIndexColor())
            .isCompleted(false)
            .todoRepeat(todoRepeatEntity)
            .build();
        todoRepository.save(newTodoEntity);
      }
      currentDate = currentDate.plusDays(1);
    }
  }

  /**
   * 투두 삭제
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @param todoId      투두ID
   * @param isDeleteAll 전체 삭제 여부
   */
  public void deleteTodo(UserDetails userDetails, Long memberId, Long todoId, boolean isDeleteAll) {
    securityUtils.validateUserDetails(userDetails, memberId);

    TodoEntity todoEntity = todoRepository.findById(todoId)
        .orElseThrow(() -> new CustomException(TODO_NOT_FOUND));

    boolean isCurrentlyRoutine = todoEntity.getTodoRepeat() != null;

    if (isCurrentlyRoutine) {
      if (isDeleteAll) {
        // 루틴 : 전체 삭제
        deleteAllRoutineTodos(todoEntity.getTodoRepeat());
      } else {
        // 루틴 : 개별 삭제
        todoRepository.delete(todoEntity);
      }
    } else {
      // 비루틴
      todoRepository.delete(todoEntity);
    }
  }

  /**
   * 루틴 전체 삭제
   *
   * @param todoRepeatEntity
   */
  private void deleteAllRoutineTodos(TodoRepeatEntity todoRepeatEntity) {
    List<TodoEntity> existingTodos = todoRepository.findAllByTodoRepeat(todoRepeatEntity);
    todoRepository.deleteAll(existingTodos);
    todoRepeatRepository.delete(todoRepeatEntity);
  }
}
