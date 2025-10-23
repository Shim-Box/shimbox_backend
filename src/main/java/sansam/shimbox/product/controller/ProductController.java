package sansam.shimbox.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sansam.shimbox.global.common.BaseResponse;
import sansam.shimbox.global.exception.ErrorCode;
import sansam.shimbox.global.security.CurrentUser;
import sansam.shimbox.global.swagger.ApiErrorCodeExamples;
import sansam.shimbox.product.dto.request.RequestProductCreateDto;
import sansam.shimbox.product.dto.response.ResponseUnassignedProductDto;
import sansam.shimbox.product.service.ProductService;

@Tag(name = "ProductController", description = "상품")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/product")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "상품 생성 API", description = "할당되지 않은 상태로 상품 생성")
    @ApiErrorCodeExamples({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.FORBIDDEN,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PostMapping("/create")
    public ResponseEntity<BaseResponse<ResponseUnassignedProductDto>> createProduct(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @RequestBody RequestProductCreateDto dto) {
        ResponseUnassignedProductDto response = productService.createProduct(userId, dto);
        return ResponseEntity.ok(BaseResponse.success(response, "상품 생성 완료", HttpStatus.OK));
    }

    @Operation(summary = "상품 삭제 API", description = "상품 ID로 기사 본인의 상품 삭제")
    @ApiErrorCodeExamples({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.FORBIDDEN,
            ErrorCode.PRODUCT_NOT_FOUND,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<BaseResponse<Void>> productDelete(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @PathVariable Long productId) {
        productService.deleteProduct(userId, productId);
        return ResponseEntity.ok(BaseResponse.success(null, "상품 삭제 완료", HttpStatus.OK));
    }

}
