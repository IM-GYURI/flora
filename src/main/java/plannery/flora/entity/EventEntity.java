package plannery.flora.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "event")
public class EventEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false)
  private LocalDateTime startDateTime;

  @Column(nullable = false)
  private LocalDateTime endDateTime;

  @Column(nullable = false)
  private String indexColor;

  @Column(nullable = false)
  private boolean isDDay;

  @ManyToOne
  @JoinColumn(name = "member_id", nullable = false)
  private MemberEntity member;

  public void updateEvent(String newTitle, String newDescription, LocalDateTime newStartDateTime,
      LocalDateTime newEndDateTime, String newIndexColor, boolean newDDay) {
    this.title = newTitle;
    this.description = newDescription;
    this.startDateTime = newStartDateTime;
    this.endDateTime = newEndDateTime;
    this.indexColor = newIndexColor;
    this.isDDay = newDDay;
  }
}
