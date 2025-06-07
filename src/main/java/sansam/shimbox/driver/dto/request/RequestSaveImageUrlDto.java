package sansam.shimbox.driver.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RequestSaveImageUrlDto {

    @Schema(description = "상품 ID", example = "1")
    private final Long productId;

    @Schema(description = "업로드된 이미지 URL", example = "https://minio.yourdomain.com/bucket/filename.jpg")
    private final String imageUrl;
}
