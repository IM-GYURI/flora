package plannery.flora.controller;

import static plannery.flora.enums.ResponseMessage.SUCCESS_TODO_COMPLETE;
import static plannery.flora.enums.ResponseMessage.SUCCESS_TODO_CREATE;
import static plannery.flora.enums.ResponseMessage.SUCCESS_TODO_DELETE;
import static plannery.flora.enums.ResponseMessage.SUCCESS_TODO_UPDATE;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
import plannery.flora.dto.todo.TodoCheckDto;
import plannery.flora.dto.todo.TodoCreateDto;
import plannery.flora.dto.todo.TodoResponseDto;
import plannery.flora.enums.TodoType;
import plannery.flora.service.TodoService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members/{memberId}/todos")
public class TodoController {

  private final TodoService todoService;

  /**
   * 투두 생성
   *
   * @param userDetails   사용자 정보
   * @param memberId      회원ID
   * @param todoCreateDto : 제목, 투두타입(TODO_STUDY, TODO_LIFE), 루틴 여부, 인덱스 색상, 시작날짜, 종료날짜, 설명, 반복 요일
   * @return "투두 생성 완료"
   */
  @PostMapping
  public ResponseEntity<String> createTodo(@AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long memberId, @RequestBody @Valid TodoCreateDto todoCreateDto) {
    todoService.createTodo(userDetails, memberId, todoCreateDto);

    return ResponseEntity.ok(SUCCESS_TODO_CREATE.getMessage());
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
  @GetMapping
  public ResponseEntity<List<TodoResponseDto>> getTodos(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long memberId, @RequestParam boolean isRoutine, @RequestParam TodoType todoType,
      @RequestParam LocalDate date) {
    return ResponseEntity.ok(
        todoService.getTodos(userDetails, memberId, isRoutine, todoType, date));
  }

  /**
   * 투두 완료 체크
   *
   * @param userDetails   사용자 정보
   * @param memberId      회원ID
   * @param todoCheckDtos 투두ID, 완료 여부 목록
   * @return "투두 완료 체크 성공"
   */
  @PutMapping("/complete")
  public ResponseEntity<String> completeTodos(@AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long memberId, @RequestBody @Valid List<TodoCheckDto> todoCheckDtos) {
    todoService.completeTodos(userDetails, memberId, todoCheckDtos);

    return ResponseEntity.ok(SUCCESS_TODO_COMPLETE.getMessage());
  }

  /**
   * 투두 개별 조회
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @param todoId      투두ID
   * @return todoCreateDto : 제목, 투두타입(TODO_STUDY, TODO_LIFE), 루틴 여부, 인덱스 색상, 시작날짜, 종료날짜, 설명, 반복 요일
   */
  @GetMapping("/{todoId}")
  public ResponseEntity<TodoCreateDto> getTodo(@AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long memberId, @PathVariable Long todoId) {
    return ResponseEntity.ok(todoService.getTodo(userDetails, memberId, todoId));
  }

  /**
   * 투두 수정
   *
   * @param userDetails   사용자 정보
   * @param memberId      회원ID
   * @param todoId        투두ID
   * @param todoCreateDto : 제목, 투두타입(TODO_STUDY, TODO_LIFE), 루틴 여부, 인덱스 색상, 시작날짜, 종료날짜, 설명, 반복 요일
   * @return "투두 수정 완료"
   */
  @PutMapping("/{todoId}")
  public ResponseEntity<String> changeTodo(@AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long memberId, @PathVariable Long todoId,
      @RequestBody @Valid TodoCreateDto todoCreateDto) {
    todoService.changeTodo(userDetails, memberId, todoId, todoCreateDto);

    return ResponseEntity.ok(SUCCESS_TODO_UPDATE.getMessage());
  }

  /**
   * 투두 삭제
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @param todoId      투두ID
   * @param isDeleteAll 전체 삭제 여부
   * @return "투두 삭제 완료"
   */
  @DeleteMapping("/{todoId}")
  public ResponseEntity<String> deleteTodo(@AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long memberId, @PathVariable Long todoId, @RequestParam boolean isDeleteAll) {
    todoService.deleteTodo(userDetails, memberId, todoId, isDeleteAll);

    return ResponseEntity.ok(SUCCESS_TODO_DELETE.getMessage());
  }
}
