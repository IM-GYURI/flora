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
public class DiaryViewDto {

  private String title;

  private String content;

  private LocalDate date;

  private String imageUrl;
}
