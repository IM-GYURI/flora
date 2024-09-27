package plannery.flora.service;

import static plannery.flora.enums.FloraType.FLORA_1;
import static plannery.flora.exception.ErrorCode.FLORA_EXISTS;
import static plannery.flora.exception.ErrorCode.FLORA_NOT_FOUND;
import static plannery.flora.exception.ErrorCode.MEMBER_NOT_FOUND;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plannery.flora.component.SecurityUtils;
import plannery.flora.dto.flora.FloraDto;
import plannery.flora.entity.FloraEntity;
import plannery.flora.entity.MemberEntity;
import plannery.flora.enums.FloraType;
import plannery.flora.exception.CustomException;
import plannery.flora.repository.FloraRepository;
import plannery.flora.repository.MemberRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class FloraService {

  private final FloraRepository floraRepository;
  private final MemberRepository memberRepository;
  private final SecurityUtils securityUtils;

  /**
   * 회원가입 시 호출 : FloraEntity 생성
   *
   * @param memberId 회원ID
   */
  public void createMyFlora(Long memberId) {
    if (floraRepository.findByMemberId(memberId).isPresent()) {
      throw new CustomException(FLORA_EXISTS);
    }

    MemberEntity member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

    FloraEntity flora = FloraEntity.builder()
        .member(member)
        .count(1)
        .floraType(FLORA_1)
        .build();

    floraRepository.save(flora);
  }

  /**
   * 로그인 시 FloraEntity 업데이트 : 마지막 updatedAt 날짜가 오늘이 아니라면 출석 체크
   *
   * @param memberId 회원ID
   */
  public void updateFloraOnLogin(Long memberId) {
    FloraEntity flora = floraRepository.findByMemberId(memberId)
        .orElseThrow(() -> new CustomException(FLORA_NOT_FOUND));

    LocalDate lastUpdatedDate = flora.getUpdatedAt().toLocalDate();
    LocalDate today = LocalDate.now();

    if (!lastUpdatedDate.equals(today)) {
      flora.updateCount(flora.getCount() + 1);
      updateFloraType(flora);
    }
  }

  /**
   * FloraEntity 업데이트 로직
   *
   * @param flora
   */
  private void updateFloraType(FloraEntity flora) {
    int count = flora.getCount();

    if (count >= 90 && flora.getFloraType() != FloraType.FLORA_4) {
      flora.updateFloraType(FloraType.FLORA_4);
    } else if (count >= 40 && flora.getFloraType() != FloraType.FLORA_3) {
      flora.updateFloraType(FloraType.FLORA_3);
    } else if (count >= 10 && flora.getFloraType() != FloraType.FLORA_2) {
      flora.updateFloraType(FloraType.FLORA_2);
    } else if (flora.getFloraType() != FloraType.FLORA_1) {
      flora.updateFloraType(FloraType.FLORA_1);
    }
  }

  /**
   * 플로롸 조회
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @return FloraDto : 카운트, 플로라 타입
   */
  public FloraDto getFlora(UserDetails userDetails, Long memberId) {
    securityUtils.validateUserDetails(userDetails, memberId);

    FloraEntity flora = floraRepository.findByMemberId(memberId)
        .orElseThrow(() -> new CustomException(FLORA_NOT_FOUND));

    return FloraDto.builder()
        .count(flora.getCount())
        .floraType(flora.getFloraType())
        .build();
  }
}
