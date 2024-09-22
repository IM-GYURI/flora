package plannery.flora.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventListByDateDto {

  private Long eventId;

  private String title;

  private LocalTime startTime;

  private LocalTime endTime;

  @JsonProperty("isAllDay")
  private boolean isAllDay;

  private String indexColor;
}
