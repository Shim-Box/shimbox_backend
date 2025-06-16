package sansam.shimbox.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.connector.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sansam.shimbox.driver.dto.response.ResponseShippingStatusDto;
import sansam.shimbox.global.common.BaseResponse;
import sansam.shimbox.global.exception.ErrorCode;
import sansam.shimbox.global.security.CurrentUser;
import sansam.shimbox.global.swagger.ApiErrorCodeExamples;
import sansam.shimbox.product.dto.request.RequestProductSaveDto;
import sansam.shimbox.product.dto.response.ResponseProductDto;
import sansam.shimbox.product.service.ProductService;

@Tag(name = "ProductController", description = "상품")
@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "상품 등록 API", description = "기사 ID와 상품 정보 등록")
    @ApiErrorCodeExamples({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.FORBIDDEN,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PostMapping("/save")
    public ResponseEntity<BaseResponse<ResponseProductDto>> productSave(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @RequestBody RequestProductSaveDto dto) {
        ResponseProductDto response = productService.saveProduct(dto);
        return ResponseEntity.ok(BaseResponse.success(response, "상품 등록 완료", HttpStatus.OK));
    }
}
