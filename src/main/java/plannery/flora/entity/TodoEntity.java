package plannery.flora.entity;

import jakarta.persistence.Column;
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
import java.time.LocalDate;
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
@Table(name = "todo")
public class TodoEntity extends BaseEntity {

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
  private LocalDate todoDate;

  @Column(nullable = false)
  private String indexColor;

  @Column(nullable = false)
  private boolean isCompleted;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "todo_repeat_id")
  private TodoRepeatEntity todoRepeat;

  public void completeCheck(boolean newIsCompleted) {
    this.isCompleted = newIsCompleted;
  }

  public void updateTodoRepeat(TodoRepeatEntity todoRepeatEntity) {
    this.todoRepeat = todoRepeatEntity;
  }

  public void updateTodo(String newTitle, TodoType newTodoType, String newIndexColor,
      LocalDate newTodoDate, String newDescription) {
    this.title = newTitle;
    this.todoType = newTodoType;
    this.indexColor = newIndexColor;
    this.todoDate = newTodoDate;
    this.description = newDescription;
  }
}
