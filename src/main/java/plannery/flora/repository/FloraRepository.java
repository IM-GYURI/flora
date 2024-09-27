package plannery.flora.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import plannery.flora.entity.FloraEntity;

@Repository
public interface FloraRepository extends JpaRepository<FloraEntity, Long> {

  Optional<FloraEntity> findByMemberId(Long memberId);

}
