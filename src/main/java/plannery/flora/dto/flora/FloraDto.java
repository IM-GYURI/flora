package plannery.flora.dto.flora;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import plannery.flora.enums.FloraType;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FloraDto {

  private int count;

  private FloraType floraType;
}
