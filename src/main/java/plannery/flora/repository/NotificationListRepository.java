package plannery.flora.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import plannery.flora.entity.NotificationListEntity;

@Repository
public interface NotificationListRepository extends JpaRepository<NotificationListEntity, Long> {

  List<NotificationListEntity> findAllByMemberId(Long memberId);

}
