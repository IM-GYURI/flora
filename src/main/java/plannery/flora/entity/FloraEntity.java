package plannery.flora.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import plannery.flora.enums.FloraType;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "flora")
public class FloraEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private int count;

  @Enumerated(EnumType.STRING)
  private FloraType floraType;

  @ManyToOne
  @JoinColumn(name = "member_id", nullable = false)
  private MemberEntity member;

  public void updateCount(int newCount) {
    this.count = newCount;
  }

  public void updateFloraType(FloraType newFloraType) {
    this.floraType = newFloraType;
  }
}
