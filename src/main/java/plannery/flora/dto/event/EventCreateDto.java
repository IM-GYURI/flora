package plannery.flora.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventCreateDto {

  @NotBlank(message = "제목은 빈 값일 수 없습니다.")
  private String title;

  private String description;

  @NotNull(message = "시작일시는 빈 값일 수 없습니다.")
  private LocalDateTime startDateTime;

  @NotNull(message = "종료일시는 빈 값일 수 없습니다.")
  private LocalDateTime endDateTime;

  @NotNull(message = "인덱스 색상 코드는 빈 값일 수 없습니다.")
  private String indexColor;

  @JsonProperty("isDDay")
  @NotNull(message = "디데이 설정 여부는 빈 값일 수 없습니다.")
  private boolean isDDay;

  @JsonProperty("isAllDay")
  @NotNull(message = "하루종일 설정 여부는 빈 값일 수 없습니다.")
  private boolean isAllDay;
}
