package plannery.flora.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import plannery.flora.enums.TodoType;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "todo_repeat")
public class TodoRepeatEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private MemberEntity member;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TodoType todoType;

  @Column(nullable = false)
  private boolean isRoutine;

  @Column(nullable = false)
  private String indexColor;

  @Column(nullable = false)
  private LocalDate startDate;

  @Column(nullable = false)
  private LocalDate endDate;

  @ElementCollection(fetch = FetchType.EAGER)
  @Enumerated(EnumType.STRING)
  private List<DayOfWeek> repeatDays;

  public void updateTodoRepeat(String newTitle, String newDescription, TodoType newTodoType,
      String newIndexColor, LocalDate newStartDate, LocalDate newEndDate,
      List<DayOfWeek> newRepeatDays) {
    this.title = newTitle;
    this.description = newDescription;
    this.todoType = newTodoType;
    this.indexColor = newIndexColor;
    this.startDate = newStartDate;
    this.endDate = newEndDate;
    this.repeatDays = newRepeatDays;
  }
}
