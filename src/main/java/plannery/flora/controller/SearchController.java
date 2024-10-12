package plannery.flora.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import plannery.flora.dto.search.SearchResultDto;
import plannery.flora.service.SearchService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {

  private final SearchService searchService;

  /**
   * 키워드 검색
   *
   * @param keyword     키워드
   * @param userDetails 사용자 정보
   * @return List<SearchResultDto> : path, title
   */
  @GetMapping
  public ResponseEntity<List<SearchResultDto>> search(@RequestParam String keyword,
      @AuthenticationPrincipal UserDetails userDetails) {
    return ResponseEntity.ok(searchService.search(userDetails, keyword));
  }
}
