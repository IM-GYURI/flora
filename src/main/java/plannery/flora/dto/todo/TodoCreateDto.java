package plannery.flora.dto.todo;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import plannery.flora.enums.TodoType;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoCreateDto {

  @NotBlank
  private String title;

  @NotNull
  private TodoType todoType;

  @NotNull
  @JsonProperty("isRoutine")
  private boolean isRoutine;

  @NotBlank
  private String indexColor;

  private LocalDate startDate;

  private LocalDate endDate;

  private String description;

  private List<DayOfWeek> repeatDays;
}
