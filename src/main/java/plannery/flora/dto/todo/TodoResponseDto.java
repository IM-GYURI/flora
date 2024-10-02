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
public class TodoResponseDto {

  private Long todoId;

  private String title;

  @JsonProperty("isCompleted")
  private boolean isCompleted;
}
