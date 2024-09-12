package plannery.flora.controller;

import static plannery.flora.enums.ResponseMessage.SUCCESS_DIARY_CREATE;
import static plannery.flora.enums.ResponseMessage.SUCCESS_DIARY_DELETE;
import static plannery.flora.enums.ResponseMessage.SUCCESS_DIARY_UPDATE;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import plannery.flora.dto.diary.DiaryCreateDto;
import plannery.flora.dto.diary.DiaryListDto;
import plannery.flora.dto.diary.DiaryViewDto;
import plannery.flora.service.DiaryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members/{memberId}")
public class DiaryController {

  private final DiaryService diaryService;

  /**
   * 일기 생성 : 해당 날짜에 이미 일기가 존재한다면 생성 불가, 이미지 파일을 첨부하지 않았다면 이미지 URL에 공백("") 저장
   *
   * @param userDetails    사용자 정보
   * @param file           이미지 파일
   * @param memberId       회원ID
   * @param diaryCreateDto : 제목, 내용, 날짜
   * @return "일기 생성 완료"
   */
  @PostMapping("/diary")
  public ResponseEntity<String> createDiary(@AuthenticationPrincipal UserDetails userDetails,
      @RequestParam("file") MultipartFile file, @PathVariable Long memberId,
      @RequestPart DiaryCreateDto diaryCreateDto) {
    diaryService.createDiary(userDetails, memberId, file, diaryCreateDto);

    return ResponseEntity.ok(SUCCESS_DIARY_CREATE.getMessage());
  }

  /**
   * 일기 개별 조회 : 존재하지 않을 경우 제목, 내용, 이미지 URL은 ""(공백), 날짜는 오늘 날짜
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @param diaryId     일기ID
   * @return DiaryViewDto : 제목, 내용, 날짜, 이미지 URL
   */
  @GetMapping("/diary/{diaryId}")
  public ResponseEntity<DiaryViewDto> getDiary(@AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long memberId, @PathVariable Long diaryId) {
    return ResponseEntity.ok(diaryService.getDiary(userDetails, memberId, diaryId));
  }

  /**
   * 일기 목록 조회
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @return List<DiaryListDto> : 일기ID, 제목, 날짜
   */
  @GetMapping("/diaries")
  public ResponseEntity<List<DiaryListDto>> getDiaries(
      @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long memberId) {
    return ResponseEntity.ok(diaryService.getDiaries(userDetails, memberId));
  }

  /**
   * 일기 수정 : 새 이미지 파일 업로드 시 기존의 이미지 파일은 S3 bucket에서 삭제
   *
   * @param userDetails    사용자 정보
   * @param memberId       회원ID
   * @param diaryId        일기ID
   * @param diaryCreateDto : 제목, 내용, 날짜
   * @param file           이미지 파일
   * @return "일기 수정 완료"
   */
  @PutMapping("/diary/{diaryId}")
  public ResponseEntity<String> updateDiary(@AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long memberId, @PathVariable Long diaryId,
      @RequestPart DiaryCreateDto diaryCreateDto, @RequestParam("file") MultipartFile file) {
    diaryService.updateDiary(userDetails, memberId, diaryId, diaryCreateDto, file);

    return ResponseEntity.ok(SUCCESS_DIARY_UPDATE.getMessage());
  }

  /**
   * 일기 삭제 : S3 bucket의 이미지도 함께 삭제
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @param diaryId     일기ID
   * @return "일기 삭제 완료"
   */
  @DeleteMapping("/diary/{diaryId}")
  public ResponseEntity<String> deleteDiary(@AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long memberId, @PathVariable Long diaryId) {
    diaryService.deleteDiary(userDetails, memberId, diaryId);

    return ResponseEntity.ok(SUCCESS_DIARY_DELETE.getMessage());
  }
}