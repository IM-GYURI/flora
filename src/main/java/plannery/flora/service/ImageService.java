package plannery.flora.service;

import static plannery.flora.enums.ImageType.IMAGE_GALLERY;
import static plannery.flora.enums.ImageType.IMAGE_PROFILE;
import static plannery.flora.exception.ErrorCode.IMAGE_NOT_FOUND;
import static plannery.flora.exception.ErrorCode.MEMBER_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import plannery.flora.component.S3ImageUpload;
import plannery.flora.component.SecurityUtils;
import plannery.flora.entity.ImageEntity;
import plannery.flora.entity.MemberEntity;
import plannery.flora.enums.ImageType;
import plannery.flora.exception.CustomException;
import plannery.flora.repository.ImageRepository;
import plannery.flora.repository.MemberRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class ImageService {

  private final S3ImageUpload s3ImageUpload;
  private final MemberRepository memberRepository;
  private final ImageRepository imageRepository;
  private final SecurityUtils securityUtils;

  @Value("${app.default.profile.url}")
  private String defaultProfileUrl;

  /**
   * 기본 이미지 설정 : 프로필 기본 이미지 URL 설정
   *
   * @param memberId 회원ID
   */
  public void createDefaultImage(Long memberId) {
    MemberEntity member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

    if (!imageRepository.existsByMemberIdAndImageType(memberId, IMAGE_PROFILE)) {
      ImageEntity defaultProfileImage = ImageEntity.builder()
          .member(member)
          .imageUrl(defaultProfileUrl)
          .imageType(IMAGE_PROFILE)
          .build();
      imageRepository.save(defaultProfileImage);
    }
  }

  /**
   * 이미지 파일 업로드 : 기존 이미지가 있다면 삭제 -> 새 이미지 업로드
   *
   * @param file      이미지 파일
   * @param memberId  회원ID
   * @param imageType 이미지 타입 : IMAGE_PROFILE, IMAGE_GALLERY
   * @return 이미지 URL
   */
  public String uploadImage(UserDetails userDetails, MultipartFile file, Long memberId,
      ImageType imageType) {
    MemberEntity member = securityUtils.validateUserDetails(userDetails, memberId);

    ImageEntity imageEntity = imageRepository.findByMemberIdAndImageType(memberId, imageType)
        .orElseGet(() -> ImageEntity.builder()
            .member(member)
            .imageType(imageType)
            .build());

    if (imageEntity.getImageUrl() != null) {
      if (imageType.equals(IMAGE_PROFILE) && !imageEntity.getImageUrl().equals(defaultProfileUrl)) {
        s3ImageUpload.deleteImage(imageEntity.getImageUrl());
      } else if (imageType.equals(IMAGE_GALLERY)) {
        s3ImageUpload.deleteImage(imageEntity.getImageUrl());
      }
    }

    String imageUrl = s3ImageUpload.uploadImage(file, memberId);

    imageEntity.updateImage(imageUrl);
    imageRepository.save(imageEntity);

    return imageUrl;
  }

  /**
   * 이미지 조회
   *
   * @param memberId  회원ID
   * @param imageType 이미지 타입 : IMAGE_PROFILE, IMAGE_GALLERY
   * @return 이미지 URL
   */
  public String getImage(UserDetails userDetails, Long memberId, ImageType imageType) {
    MemberEntity member = securityUtils.validateUserDetails(userDetails, memberId);

    return imageRepository.findByMemberIdAndImageType(memberId, imageType)
        .map(ImageEntity::getImageUrl)
        .orElse(null);
  }

  /**
   * 이미지 삭제 시 기본 이미지로 재설정
   *
   * @param memberId  회원ID
   * @param imageType 이미지 타입 : IMAGE_PROFILE, IMAGE_GALLERY
   */
  public void deleteImage(UserDetails userDetails, Long memberId, ImageType imageType) {
    securityUtils.validateUserDetails(userDetails, memberId);

    ImageEntity imageEntity = imageRepository.findByMemberIdAndImageType(memberId, imageType)
        .orElseThrow(() -> new CustomException(IMAGE_NOT_FOUND));

    if (imageType.equals(IMAGE_PROFILE)) {
      if (!imageEntity.getImageUrl().equals(defaultProfileUrl)) {
        s3ImageUpload.deleteImage(imageEntity.getImageUrl());
        imageEntity.updateImage(defaultProfileUrl);
        imageRepository.save(imageEntity);
      }
    } else {
      s3ImageUpload.deleteImage(imageEntity.getImageUrl());
      imageRepository.save(imageEntity);
    }
  }
}
