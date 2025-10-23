package sansam.shimbox.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RequestProductAssignDto {

    @Schema(description = "상품 ID", example = "1")
    @NotNull
    private Long productId;

    @Schema(description = "기사 ID", example = "1")
    @NotNull
    private Long driverId;
}
