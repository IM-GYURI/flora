package plannery.flora.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import plannery.flora.entity.ImageEntity;
import plannery.flora.enums.ImageType;

@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, Long> {

  Optional<ImageEntity> findByMemberIdAndImageType(Long memberId, ImageType imageType);

  boolean existsByMemberIdAndImageType(Long memberId, ImageType imageType);
}
