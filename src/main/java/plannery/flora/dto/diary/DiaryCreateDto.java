package plannery.flora.dto.diary;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaryCreateDto {

  @NotBlank(message = "제목은 빈 값일 수 없습니다.")
  private String title;

  private String content;

  @NotNull(message = "날짜는 필수 입력값입니다.")
  private LocalDate date;
}
