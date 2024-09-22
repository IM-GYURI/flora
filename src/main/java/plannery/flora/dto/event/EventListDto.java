package plannery.flora.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventListDto {

  private Long eventId;

  private String title;

  private LocalDateTime startDateTime;

  private LocalDateTime endDateTime;

  private String indexColor;

  @JsonProperty("isAllDay")
  private boolean isAllDay;
}
