package plannery.flora.service;

import static plannery.flora.exception.ErrorCode.PROMISE_EXISTS;
import static plannery.flora.exception.ErrorCode.PROMISE_NOT_FOUND;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plannery.flora.component.SecurityUtils;
import plannery.flora.dto.promise.PromiseDto;
import plannery.flora.entity.MemberEntity;
import plannery.flora.entity.PromiseEntity;
import plannery.flora.exception.CustomException;
import plannery.flora.repository.PromiseRepository;

@Service
@RequiredArgsConstructor
public class PromiseService {

  private final PromiseRepository promiseRepository;
  private final SecurityUtils securityUtils;

  /**
   * 다짐 생성
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @param promiseDto: content
   */
  @Transactional
  public void createPromise(UserDetails userDetails, Long memberId, PromiseDto promiseDto) {
    MemberEntity member = securityUtils.validateUserDetails(userDetails, memberId);

    Optional<PromiseEntity> existingPromise = promiseRepository.findByMemberId(memberId);
    if (existingPromise.isPresent()) {
      throw new CustomException(PROMISE_EXISTS);
    }

    PromiseEntity promise = PromiseEntity.builder()
        .member(member)
        .content(promiseDto.getContent())
        .build();

    promiseRepository.save(promise);
  }

  /**
   * 다짐 조회
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @return PromiseDto : 내용
   */
  public PromiseDto getPromise(UserDetails userDetails, Long memberId) {
    securityUtils.validateUserDetails(userDetails, memberId);

    Optional<PromiseEntity> promise = promiseRepository.findByMemberId(memberId);

    return promise.map(promiseEntity -> PromiseDto.builder()
        .content(promiseEntity.getContent())
        .build()).orElse(PromiseDto.builder().content("").build());
  }

  /**
   * 다짐 수정
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   * @param promiseDto  : 내용
   */
  @Transactional
  public void updatePromise(UserDetails userDetails, Long memberId, PromiseDto promiseDto) {
    securityUtils.validateUserDetails(userDetails, memberId);

    PromiseEntity promise = promiseRepository.findByMemberId(memberId)
        .orElseThrow(() -> new CustomException(PROMISE_NOT_FOUND));

    promise.updateContent(promiseDto.getContent());
  }

  /**
   * 다짐 삭제
   *
   * @param userDetails 사용자 정보
   * @param memberId    회원ID
   */
  @Transactional
  public void deletePromise(UserDetails userDetails, Long memberId) {
    securityUtils.validateUserDetails(userDetails, memberId);

    PromiseEntity promise = promiseRepository.findByMemberId(memberId)
        .orElseThrow(() -> new CustomException(PROMISE_NOT_FOUND));

    promiseRepository.deleteById(promise.getId());
  }
}