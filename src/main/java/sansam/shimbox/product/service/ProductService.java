package sansam.shimbox.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sansam.shimbox.auth.domain.User;
import sansam.shimbox.auth.enums.Role;
import sansam.shimbox.auth.repository.UserRepository;
import sansam.shimbox.driver.domain.Driver;
import sansam.shimbox.driver.repository.DriverRepository;
import sansam.shimbox.global.exception.CustomException;
import sansam.shimbox.global.exception.ErrorCode;
import sansam.shimbox.product.domain.Product;
import sansam.shimbox.product.domain.ProductTimeLine;
import sansam.shimbox.product.dto.request.RequestProductSaveDto;
import sansam.shimbox.product.dto.response.ResponseProductDto;
import sansam.shimbox.product.dto.response.ResponseProductTimelineDto;
import sansam.shimbox.product.enums.ShippingStatus;
import sansam.shimbox.product.repository.ProductRepository;
import sansam.shimbox.product.repository.ProductTimelineRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductTimelineRepository productTimelineRepository;
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;

    @Transactional
    public ResponseProductDto saveProduct(Long userId, RequestProductSaveDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!user.getRole().equals(Role.ADMIN)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        Driver driver = driverRepository.findById(dto.getDriverId())
                .orElseThrow(() -> new CustomException(ErrorCode.DRIVER_NOT_FOUND));

        Product product = Product.builder()
                .productName(dto.getProductName())
                .recipientName(dto.getRecipientName())
                .recipientPhoneNumber(dto.getRecipientPhoneNumber())
                .address(dto.getAddress())
                .detailAddress(dto.getDetailAddress())
                .postalCode(dto.getPostalCode())
                .shippingStatus(dto.getShippingStatus())
                .driver(driver)
                .isDeleted(false)
                .build();

        Product saved = productRepository.save(product);

        return ResponseProductDto.builder()
                .productId(saved.getProductId())
                .productName(saved.getProductName())
                .recipientName(saved.getRecipientName())
                .recipientPhoneNumber(saved.getRecipientPhoneNumber())
                .address(saved.getAddress())
                .detailAddress(saved.getDetailAddress())
                .postalCode(saved.getPostalCode())
                .shippingStatus(saved.getShippingStatus())
                .driverId(saved.getDriver().getDriverId())
                .build();
    }

    @Transactional
    public void deleteProduct(Long userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!user.getRole().equals(Role.ADMIN)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.SHIPP_NOT_FOUND));

        productRepository.delete(product);
    }

    // 상품 타임라인 조회 (관리자용)
    @Transactional(readOnly = true)
    public List<ResponseProductTimelineDto> getProductTimeline(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.SHIPP_NOT_FOUND));

        List<ProductTimeLine> timelines = productTimelineRepository.findByProductOrderByStatusChangedAtAsc(product);

        return timelines.stream()
                .map(ResponseProductTimelineDto::from)
                .toList();
    }

    /**
     * 상품 상태 변경 시 위치와 시간을 DB에 저장
     * 
     * @param productId 상품 ID
     * @param status 변경된 배송 상태
     * @param latitude 위도
     * @param longitude 경도
     * @param addressShort 간단 주소
     */
    @Transactional
    public void saveProductStatusChangeLocation(Long productId, ShippingStatus status, 
                                             Double latitude, Double longitude, String addressShort) {
        if (latitude != null && longitude != null) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new CustomException(ErrorCode.SHIPP_NOT_FOUND));

            ProductTimeLine timeline = ProductTimeLine.builder()
                    .product(product)
                    .status(status)
                    .statusChangedAt(LocalDateTime.now())
                    .latitude(latitude)
                    .longitude(longitude)
                    .addressShort(addressShort)
                    .isDeleted(false)
                    .build();

            productTimelineRepository.save(timeline);
        }
    }
}
