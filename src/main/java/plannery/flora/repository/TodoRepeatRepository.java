package plannery.flora.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import plannery.flora.entity.TodoRepeatEntity;

@Repository
public interface TodoRepeatRepository extends JpaRepository<TodoRepeatEntity, Long> {

}
