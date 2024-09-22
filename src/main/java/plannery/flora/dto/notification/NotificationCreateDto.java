package plannery.flora.dto.notification;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCreateDto {

  @NotBlank(message = "메세지는 필수 입력값입니다.")
  private String message;
}
