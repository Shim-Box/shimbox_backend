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
import sansam.shimbox.product.dto.request.RequestProductCreateDto;
import sansam.shimbox.product.dto.request.RequestProductAssignDto;
import sansam.shimbox.product.dto.response.ResponseProductDto;
import sansam.shimbox.product.dto.response.ResponseUnassignedProductDto;
import sansam.shimbox.product.dto.response.ResponseProductTimelineDto;
import sansam.shimbox.product.enums.ShippingStatus;
import sansam.shimbox.product.repository.ProductRepository;
import sansam.shimbox.product.repository.ProductTimelineRepository;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductTimelineRepository productTimelineRepository;
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;

    /**
     * 상품 생성 (할당되지 않은 상태)
     */
    @Transactional
    public ResponseUnassignedProductDto createProduct(Long userId, RequestProductCreateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!user.getRole().equals(Role.ADMIN)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        Product product = Product.builder()
                .productName(dto.getProductName())
                .recipientName(dto.getRecipientName())
                .recipientPhoneNumber(dto.getRecipientPhoneNumber())
                .address(dto.getAddress())
                .detailAddress(dto.getDetailAddress())
                .postalCode(dto.getPostalCode())
                .shippingStatus(null)
                .driver(null)
                .isDeleted(false)
                .build();

        Product saved = productRepository.save(product);

        return ResponseUnassignedProductDto.from(saved);
    }

    /**
     * 기사에게 상품 할당
     */
    @Transactional
    public ResponseProductDto assignProduct(Long userId, RequestProductAssignDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!user.getRole().equals(Role.ADMIN)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 상품 조회
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 이미 할당된 상품인지 확인
        if (product.getDriver() != null) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 기사 조회
        Driver driver = driverRepository.findById(dto.getDriverId())
                .orElseThrow(() -> new CustomException(ErrorCode.DRIVER_NOT_FOUND));

        // 기사에게 배정된 모든 상품 조회
        List<Product> allProducts = productRepository.findByDriverAndIsDeletedFalse(driver);
        
        // 현재 최신 라운드 찾기 (Product의 assignedRound 최댓값)
        Integer currentMaxRound = allProducts.stream()
                .filter(p -> p.getAssignedRound() != null)
                .mapToInt(Product::getAssignedRound)
                .max()
                .orElse(0);
        
        // 새로운 라운드 결정
        Integer newRound;
        if (currentMaxRound == 0) {
            // 첫 배정인 경우 라운드 1
            newRound = 1;
        } else {
            // 최신 라운드의 미완료 상품 확인
            List<Product> currentRoundProducts = productRepository.findByDriverAndShippingStatusInAndIsDeletedFalseAndAssignedRound(
                    driver,
                    List.of(ShippingStatus.WAITING, ShippingStatus.STARTED),
                    currentMaxRound
            );
            
            // 최신 라운드에 미완료 상품이 없으면 새로운 라운드 시작
            if (currentRoundProducts.isEmpty()) {
                newRound = currentMaxRound + 1;
            } else {
                // 최신 라운드에 미완료 상품이 있으면 같은 라운드 사용
                newRound = currentMaxRound;
            }
        }

        // 상품 업데이트 (새로운 라운드로 배정)
        product.assignToDriver(driver, newRound);

        Product saved = productRepository.save(product);

        return ResponseProductDto.from(saved);
    }

    /**
     * 상품 삭제
     */
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

            // 한국 시간대 문자열로 변환하여 저장
            String kstTimeString = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));

            ProductTimeLine timeline = ProductTimeLine.builder()
                    .product(product)
                    .status(status)
                    .statusChangedAt(kstTimeString)
                    .latitude(latitude)
                    .longitude(longitude)
                    .addressShort(addressShort)
                    .isDeleted(false)
                    .build();

            productTimelineRepository.save(timeline);
        }
    }
}
