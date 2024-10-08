package plannery.flora.dto.timer;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimerCreateDto {

  @NotNull
  private Long todoId;

  @NotNull
  private long duration;
}
