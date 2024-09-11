package plannery.flora.controller;

import static plannery.flora.enums.ResponseMessage.NO_IMAGE_FILE;
import static plannery.flora.enums.ResponseMessage.SUCCESS_IMAGE_DELETE;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import plannery.flora.enums.ImageType;
import plannery.flora.service.ImageService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members/{memberId}/image")
public class ImageController {

  private final ImageService imageService;

  /**
   * 이미지 업로드
   *
   * @param file      이미지 파일
   * @param memberId  회원ID
   * @param imageType 이미지 타입 : IMAGE_PROFILE, IMAGE_GALLERY
   * @return 이미지 URL
   */
  @PostMapping
  public ResponseEntity<String> uploadImage(@AuthenticationPrincipal UserDetails userdetails,
      @RequestParam("file") MultipartFile file, @PathVariable Long memberId,
      @RequestParam("imageType") ImageType imageType) {
    return ResponseEntity.ok(imageService.uploadImage(userdetails, file, memberId, imageType));
  }

  /**
   * 이미지 조회
   *
   * @param memberId  회원ID
   * @param imageType 이미지 타입 : IMAGE_PROFILE, IMAGE_GALLERY
   * @return 이미지가 존재하는 경우 -> 이미지 URL, 이미지가 존재하지 않는 경우 -> "이미지 파일 부재"
   */
  @GetMapping
  public ResponseEntity<String> getImage(@AuthenticationPrincipal UserDetails userdetails,
      @PathVariable Long memberId, @RequestParam("imageType") ImageType imageType) {
    String imageUrl = imageService.getImage(userdetails, memberId, imageType);
    return imageUrl != null ? ResponseEntity.ok(imageUrl)
        : ResponseEntity.ok(NO_IMAGE_FILE.getMessage());
  }

  /**
   * 이미지 삭제
   *
   * @param memberId  회원ID
   * @param imageType 이미지 타입 : IMAGE_PROFILE, IMAGE_GALLERY
   * @return "이미지 파일 삭제 완료"
   */
  @DeleteMapping
  public ResponseEntity<String> deleteImage(@AuthenticationPrincipal UserDetails userdetails,
      @PathVariable Long memberId, @RequestParam("imageType") ImageType imageType) {
    imageService.deleteImage(userdetails, memberId, imageType);
    return ResponseEntity.ok(SUCCESS_IMAGE_DELETE.getMessage());
  }
}
