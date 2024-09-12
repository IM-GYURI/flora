package plannery.flora.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import plannery.flora.entity.DiaryEntity;

@Repository
public interface DiaryRepository extends JpaRepository<DiaryEntity, Long> {

  @Query("SELECT d FROM DiaryEntity d WHERE d.member.id = :memberId")
  List<DiaryEntity> findAllByMemberId(@Param("memberId") Long memberId);

  @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM DiaryEntity d WHERE d.member.id = :memberId AND d.date = :date")
  boolean existsByMemberIdAndDate(@Param("memberId") Long memberId, @Param("date") LocalDate date);
}
