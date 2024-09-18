package plannery.flora.component;

import static plannery.flora.exception.ErrorCode.FAILED_TO_DELETE_IMAGE;
import static plannery.flora.exception.ErrorCode.FILE_SIZE_EXCEEDED;
import static plannery.flora.exception.ErrorCode.INVALID_FILE_FORMAT;
import static plannery.flora.exception.ErrorCode.INVALID_IMAGE_URL;
import static plannery.flora.exception.ErrorCode.S3_UPLOAD_ERROR;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import plannery.flora.exception.CustomException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@RequiredArgsConstructor
public class S3ImageUpload {

  private final S3Client s3Client;

  @Value("${cloud.aws.s3.bucket}")
  private String bucketName;

  // 허용하는 파일 확장자 목록
  private final List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png");

  // 최대 파일 크기 (10MB)
  private final long maxFileSize = 10 * 1024 * 1024;

  /**
   * 이미지 파일 업로드
   *
   * @param file      이미지 파일
   * @param directory 파일을 저장할 디렉토리 이름
   * @return S3 Url
   */
  public String uploadImage(MultipartFile file, Long directory) {
    // 확장자 검사
    String originalFilename = file.getOriginalFilename();
    String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1)
        .toLowerCase();

    if (!allowedExtensions.contains(extension)) {
      throw new CustomException(INVALID_FILE_FORMAT);
    }

    // 파일 크기 검사
    if (file.getSize() > maxFileSize) {
      throw new CustomException(FILE_SIZE_EXCEEDED);
    }

    String fileName = UUID.randomUUID() + "." + extension;
    String key = directory + "/" + fileName;

    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .contentType(file.getContentType())
        .build();

    try {
      s3Client.putObject(putObjectRequest,
          RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
    } catch (IOException e) {
      throw new CustomException(S3_UPLOAD_ERROR);
    }

    return String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);
  }

  /**
   * 이미지 파일 삭제
   *
   * @param imageUrl 이미지 Url
   */
  public void deleteImage(String imageUrl) {
    try {
      String key = extractKeyFromUrl(imageUrl);
      DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
          .bucket(bucketName)
          .key(key)
          .build();

      s3Client.deleteObject(deleteObjectRequest);
    } catch (Exception e) {
      throw new CustomException(FAILED_TO_DELETE_IMAGE);
    }
  }

  private String extractKeyFromUrl(String imageUrl) {
    String bucketUrl = String.format("https://%s.s3.amazonaws.com/", bucketName);

    if (imageUrl.startsWith(bucketUrl)) {
      return imageUrl.substring(bucketUrl.length());
    } else {
      throw new CustomException(INVALID_IMAGE_URL);
    }
  }

  /**
   * 회원과 관련된 이미지 전체 삭제
   *
   * @param memberId 회원ID
   */
  public void deleteAllImages(Long memberId) {
    String folderKey = memberId + "/";

    ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
        .bucket(bucketName)
        .prefix(folderKey)
        .build();

    ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request);

    listObjectsV2Response.contents().forEach(s3Object -> {
      String key = s3Object.key();

      DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
          .bucket(bucketName)
          .key(key)
          .build();

      s3Client.deleteObject(deleteObjectRequest);
    });
  }
}
