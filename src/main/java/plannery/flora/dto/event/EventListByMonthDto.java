package plannery.flora.dto.event;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventListByMonthDto {

  private Long eventId;

  private String title;

  private LocalDate startDate;

  private LocalDate endDate;

  private String indexColor;
}
