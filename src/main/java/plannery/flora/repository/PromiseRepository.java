package plannery.flora.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import plannery.flora.entity.PromiseEntity;

@Repository
public interface PromiseRepository extends JpaRepository<PromiseEntity, Long> {

  @Query("SELECT p FROM PromiseEntity p WHERE p.member.id = :memberId")
  Optional<PromiseEntity> findByMemberId(@Param("memberId") Long memberId);
}
