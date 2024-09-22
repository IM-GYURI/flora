package plannery.flora.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import plannery.flora.entity.EventEntity;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long> {

  List<EventEntity> findAllByMemberId(Long memberId);


  List<EventEntity> findAllByMemberIdAndStartDateTimeLessThanEqualAndEndDateTimeGreaterThanEqualOrStartDateTimeBetweenOrEndDateTimeBetween(
      Long memberId, LocalDateTime startOfDay, LocalDateTime endOfDay,
      LocalDateTime startRange1, LocalDateTime endRange1,
      LocalDateTime startRange2, LocalDateTime endRange2);


  @Query("SELECT e FROM EventEntity e WHERE e.member.id = :memberId " +
      "AND (e.startDateTime <= :endDate AND e.endDateTime >= :startDate)")
  List<EventEntity> findEventsForMemberWithinMonth(@Param("memberId") Long memberId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query("SELECT e FROM EventEntity e WHERE e.member.id = :memberId AND e.isDDay = true AND e.startDateTime >= :todayStartOfDay")
  List<EventEntity> findDDayEventsByMemberId(@Param("memberId") Long memberId,
      @Param("todayStartOfDay") LocalDateTime todayStartOfDay);
}
