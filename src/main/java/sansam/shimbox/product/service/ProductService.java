package sansam.shimbox.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sansam.shimbox.driver.domain.Driver;
import sansam.shimbox.driver.repository.DriverRepository;
import sansam.shimbox.global.exception.CustomException;
import sansam.shimbox.global.exception.ErrorCode;
import sansam.shimbox.product.domain.Product;
import sansam.shimbox.product.dto.request.RequestProductSaveDto;
import sansam.shimbox.product.dto.response.ResponseProductDto;
import sansam.shimbox.product.repository.ProductRepository;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final DriverRepository driverRepository;

    @Transactional
    public ResponseProductDto saveProduct(RequestProductSaveDto dto) {
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
}
