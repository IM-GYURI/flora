package plannery.flora.dto.diary;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaryListDto {

  private Long diaryId;

  private String title;

  private LocalDate date;
}
