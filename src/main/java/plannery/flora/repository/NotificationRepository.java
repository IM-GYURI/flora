package plannery.flora.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import plannery.flora.entity.NotificationEntity;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

  List<NotificationEntity> findAllByEventIdAfter(String lastEventId);

}
