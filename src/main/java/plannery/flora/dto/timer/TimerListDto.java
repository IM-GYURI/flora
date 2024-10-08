package plannery.flora.dto.timer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimerListDto {

  private Long todoId;

  private String title;

  private Long timerId;

  private long duration;
}
