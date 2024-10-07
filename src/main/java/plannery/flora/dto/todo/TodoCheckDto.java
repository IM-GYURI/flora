package plannery.flora.dto.todo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoCheckDto {

  private Long todoId;

  @JsonProperty("isCompleted")
  private boolean isCompleted;
}
