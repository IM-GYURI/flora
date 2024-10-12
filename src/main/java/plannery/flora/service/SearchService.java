package plannery.flora.service;

import static plannery.flora.exception.ErrorCode.MEMBER_NOT_FOUND;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import plannery.flora.dto.search.SearchResultDto;
import plannery.flora.entity.DiaryEntity;
import plannery.flora.entity.EventEntity;
import plannery.flora.entity.MemberEntity;
import plannery.flora.entity.PromiseEntity;
import plannery.flora.entity.TodoEntity;
import plannery.flora.exception.CustomException;
import plannery.flora.repository.DiaryRepository;
import plannery.flora.repository.EventRepository;
import plannery.flora.repository.MemberRepository;
import plannery.flora.repository.PromiseRepository;
import plannery.flora.repository.TodoRepository;

@Service
@RequiredArgsConstructor
public class SearchService {

  private final DiaryRepository diaryRepository;
  private final EventRepository eventRepository;
  private final PromiseRepository promiseRepository;
  private final TodoRepository todoRepository;
  private final MemberRepository memberRepository;

  /**
   * 키워드 검색 : 일기, 이벤트, 목표/다짐, 투두
   *
   * @param userDetails 사용자 정보
   * @param keyword     키워드
   * @return List<SearchResultDto> : path, title
   */
  public List<SearchResultDto> search(UserDetails userDetails, String keyword) {
    MemberEntity member = memberRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

    Long memberId = member.getId();

    List<SearchResultDto> results = new ArrayList<>();

    // 일기
    List<DiaryEntity> diaries = diaryRepository.findByMemberIdAndTitleContainingOrMemberIdAndContentContaining(
        memberId, keyword, memberId, keyword);
    for (DiaryEntity diary : diaries) {
      results.add(SearchResultDto.builder()
          .path("캘린더 > 오늘의 일기")
          .title(diary.getTitle())
          .id(diary.getId())
          .build());
    }

    // 이벤트
    List<EventEntity> events = eventRepository.findByMemberIdAndTitleContainingOrMemberIdAndDescriptionContaining(
        memberId, keyword, memberId, keyword);
    for (EventEntity event : events) {
      results.add(SearchResultDto.builder()
          .path("캘린더 > 이벤트")
          .title(event.getTitle())
          .id(event.getId())
          .build());
    }

    // 목표/다짐
    List<PromiseEntity> promises = promiseRepository.findByMemberIdAndContentContaining(memberId,
        keyword);
    for (PromiseEntity promise : promises) {
      results.add(SearchResultDto.builder()
          .path("대시보드 > 목표/다짐")
          .title(promise.getContent())
          .id(promise.getId())
          .build());
    }

    // 투두
    List<TodoEntity> todos = todoRepository.findByMemberIdAndTitleContainingOrMemberIdAndDescriptionContaining(
        memberId, keyword, memberId, keyword);
    for (TodoEntity todo : todos) {
      results.add(SearchResultDto.builder()
          .path("캘린더 > Todolist")
          .title(todo.getTitle())
          .id(todo.getId())
          .build());
    }

    return results;
  }
}
