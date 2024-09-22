package plannery.flora.dto.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationListDto {

  private String message;
  
  private LocalDate date;

  @JsonProperty("isRead")
  private boolean isRead;
}
