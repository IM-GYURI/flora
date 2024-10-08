package plannery.flora.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import plannery.flora.entity.TimerEntity;
import plannery.flora.entity.TodoEntity;

@Repository
public interface TimerRepository extends JpaRepository<TimerEntity, Long> {

  Optional<TimerEntity> findByTodo(TodoEntity todo);
}
