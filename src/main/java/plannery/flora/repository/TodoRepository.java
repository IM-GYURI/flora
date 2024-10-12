package plannery.flora.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import plannery.flora.entity.TodoEntity;
import plannery.flora.entity.TodoRepeatEntity;
import plannery.flora.enums.TodoType;

@Repository
public interface TodoRepository extends JpaRepository<TodoEntity, Long> {

  List<TodoEntity> findAllByTodoRepeat(TodoRepeatEntity todoRepeatEntity);

  @Query("SELECT t FROM TodoEntity t " +
      "WHERE t.member.id = :memberId " +
      "AND t.todoType = :todoType " +
      "AND t.todoDate = :date " +
      "AND (:isRoutine IS TRUE AND t.todoRepeat IS NOT NULL OR :isRoutine IS FALSE AND t.todoRepeat IS NULL)")
  List<TodoEntity> findTodosByCriteria(@Param("memberId") Long memberId,
      @Param("todoType") TodoType todoType,
      @Param("date") LocalDate date,
      @Param("isRoutine") boolean isRoutine);

  List<TodoEntity> findAllById(Iterable<Long> ids);

  @Query("SELECT t FROM TodoEntity t WHERE t.member.id = :memberId AND t.todoDate = :today")
  List<TodoEntity> findTodosByDate(@Param("memberId") Long memberId,
      @Param("today") LocalDate today);

  List<TodoEntity> findByMemberIdAndTitleContainingOrMemberIdAndDescriptionContaining(
      Long memberId1, String keyword1, Long memberId2, String keyword2);
}
