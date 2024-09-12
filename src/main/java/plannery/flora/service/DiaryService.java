package plannery.flora.service;

import static plannery.flora.exception.ErrorCode.DIARY_EXISTS;
import static plannery.flora.exception.ErrorCode.DIARY_NOT_FOUND;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import plannery.flora.component.S3ImageUpload;
import plannery.flora.component.SecurityUtils;
import plannery.flora.dto.diary.DiaryCreateDto;
import plannery.flora.dto.diary.DiaryListDto;
import plannery.flora.dto.diary.DiaryViewDto;
import plannery.flora.entity.DiaryEntity;
import plannery.flora.entity.MemberEntity;
import plannery.flora.exception.CustomException;
import plannery.flora.repository.DiaryRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class DiaryService {

  private final SecurityUtils securityUtils;
  private final S3ImageUpload s3ImageUpload;
  private final DiaryRepository diaryRepository;

  /**
   * 일기 생성 : 해당 날짜에 이미 일기가 존재한다면 생성 불가, 이미지 파일을 첨부하지 않았다면 이미지 URL에 공백("") 저장
   *
   * @param userDetails    사용자 정보
   * @param memberId       회원ID
   * @param file           이미지 파일
   * @param diaryCreateDto : 제목, 내용, 날짜
   */
  public void createDiary(UserDetails userDetails, Long memberId, MultipartFile file,
      DiaryCreateDto diaryCreateDto) {
    MemberEntity member = securityUtils.validateUserDetails(userDetails, memberId);

    boolean isExist = diaryRepository.existsByMemberIdAndDate(memberId, diaryCreateDto.getDate());

    if (isExist) {
      throw new CustomException(DIARY_EXISTS);
    }

    String imageUrl =
        (file != null && !file.isEmpty()) ? s3ImageUpload.uploadImage(file, memberId) : "";

    DiaryEntity diary = DiaryEntity.builder()
        .member(member)
        .title(diaryCreateDto.getTitle())
        .content(diaryCreateDto.getContent())
        .date(diaryCreateDto.getDate())
        .imageUrl(imageUrl)
        .build();

    diaryRepository.save(diary);
  }

  /**
   * 일기 개별 조회 : 존재하지 않을 경우 제목, 내용, 이미지 URL은 ""(공백), 날짜는 오늘 날짜
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @param diaryId     일기ID
   * @return DiaryViewDto : 제목, 내용, 날짜, 이미지 URL
   */
  public DiaryViewDto getDiary(UserDetails userDetails, Long memberId, Long diaryId) {
    securityUtils.validateUserDetails(userDetails, memberId);

    DiaryEntity diary = diaryRepository.findById(diaryId).orElse(null);

    if (diary == null) {
      return DiaryViewDto.builder()
          .title("")
          .content("")
          .date(LocalDate.now())
          .imageUrl("")
          .build();
    }

    return DiaryViewDto.builder()
        .title(diary.getTitle())
        .content(diary.getContent())
        .date(diary.getDate())
        .imageUrl(diary.getImageUrl())
        .build();
  }

  /**
   * 일기 목록 조회
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @return List<DiaryListDto> : 일기ID, 제목, 날짜
   */
  public List<DiaryListDto> getDiaries(UserDetails userDetails, Long memberId) {
    securityUtils.validateUserDetails(userDetails, memberId);

    List<DiaryEntity> diaries = diaryRepository.findAllByMemberId(memberId);

    return diaries.stream()
        .map(diary -> DiaryListDto.builder()
            .diaryId(diary.getId())
            .title(diary.getTitle())
            .date(diary.getDate())
            .build())
        .toList();
  }

  /**
   * 일기 수정 : 새 이미지 파일 업로드 시 기존의 이미지 파일은 S3 bucket에서 삭제
   *
   * @param userDetails    사용자 정보
   * @param memberId       회원ID
   * @param diaryId        일기ID
   * @param diaryCreateDto : 제목, 내용, 날짜
   * @param file           이미지 파일
   */
  public void updateDiary(UserDetails userDetails, Long memberId, Long diaryId,
      DiaryCreateDto diaryCreateDto, MultipartFile file) {
    securityUtils.validateUserDetails(userDetails, memberId);

    DiaryEntity diary = diaryRepository.findById(diaryId)
        .orElseThrow(() -> new CustomException(DIARY_NOT_FOUND));

    String imageUrl = diary.getImageUrl();

    if (file != null && !file.isEmpty()) {
      if (imageUrl != null && !imageUrl.isEmpty()) {
        s3ImageUpload.deleteImage(imageUrl);
      }

      imageUrl = s3ImageUpload.uploadImage(file, memberId);
    }

    diary.updateDiary(diaryCreateDto.getTitle(), diaryCreateDto.getContent(),
        diaryCreateDto.getDate(), imageUrl);
  }

  /**
   * 일기 삭제 : S3 bucket의 이미지도 함께 삭제
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @param diaryId     일기ID
   */
  public void deleteDiary(UserDetails userDetails, Long memberId, Long diaryId) {
    securityUtils.validateUserDetails(userDetails, memberId);

    DiaryEntity diary = diaryRepository.findById(diaryId)
        .orElseThrow(() -> new CustomException(DIARY_NOT_FOUND));

    if (diary.getImageUrl() != null && !diary.getImageUrl().isEmpty()) {
      s3ImageUpload.deleteImage(diary.getImageUrl());
    }

    diaryRepository.delete(diary);
  }
}
