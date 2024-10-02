package plannery.flora.controller;

import static plannery.flora.enums.ResponseMessage.SUCCESS_PROMISE_CREATE;
import static plannery.flora.enums.ResponseMessage.SUCCESS_PROMISE_DELETE;
import static plannery.flora.enums.ResponseMessage.SUCCESS_PROMISE_UPDATE;

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
import org.springframework.web.bind.annotation.RestController;
import plannery.flora.dto.promise.PromiseDto;
import plannery.flora.service.PromiseService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members/{memberId}/promises")
public class PromiseController {

  private final PromiseService promiseService;

  /**
   * 다짐 생성
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @param promiseDto  : 내용
   * @return "다짐 생성 완료"
   */
  @PostMapping
  public ResponseEntity<String> createPromise(@AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long memberId, @RequestBody PromiseDto promiseDto) {
    promiseService.createPromise(userDetails, memberId, promiseDto);

    return ResponseEntity.ok(SUCCESS_PROMISE_CREATE.getMessage());
  }

  /**
   * 다짐 조회
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @return PromiseDto : 내용
   */
  @GetMapping
  public ResponseEntity<PromiseDto> getPromise(@AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long memberId) {
    return ResponseEntity.ok(promiseService.getPromise(userDetails, memberId));
  }

  /**
   * 다짐 수정
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @param promiseDto  : 내용
   * @return "다짐 수정 완료"
   */
  @PutMapping
  public ResponseEntity<String> updatePromise(@AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long memberId, @RequestBody PromiseDto promiseDto) {
    promiseService.updatePromise(userDetails, memberId, promiseDto);

    return ResponseEntity.ok(SUCCESS_PROMISE_UPDATE.getMessage());
  }

  /**
   * 다짐 삭제
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @return "다짐 삭제 완료"
   */
  @DeleteMapping
  public ResponseEntity<String> deletePromise(@AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long memberId) {
    promiseService.deletePromise(userDetails, memberId);

    return ResponseEntity.ok(SUCCESS_PROMISE_DELETE.getMessage());
  }
}
