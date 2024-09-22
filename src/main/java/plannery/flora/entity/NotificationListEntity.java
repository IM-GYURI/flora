package plannery.flora.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notification_list")
public class NotificationListEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private boolean isRead;

  @ManyToOne
  @JoinColumn(name = "member_id", nullable = false)
  private MemberEntity member;

  @ManyToOne
  @JoinColumn(name = "notification_id", nullable = false)
  private NotificationEntity notification;

  public void updateRead(boolean isRead) {
    this.isRead = isRead;
  }
}
