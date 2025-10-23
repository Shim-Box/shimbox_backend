package sansam.shimbox.product.domain;

import jakarta.persistence.*;
import lombok.*;
import sansam.shimbox.driver.domain.Driver;
import sansam.shimbox.global.common.BaseTimeEntity;
import sansam.shimbox.product.enums.ShippingStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_PRODUCT")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name", length = 100, nullable = false)
    private String productName;

    @Column(name = "recipient_name", length = 50, nullable = false)
    private String recipientName;

    @Column(name = "recipient_phone_number", length = 20, nullable = false)
    private String recipientPhoneNumber;

    @Column(nullable = false, length = 50)
    private String address;

    @Column(name = "detail_address", nullable = false)
    private String detailAddress;

    @Column(name = "postal_code", length = 10, nullable = false)
    private String postalCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipping_status")
    private ShippingStatus shippingStatus;

    @Column(name = "delivery_image_url")
    private String deliveryImageUrl;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "delete_date")
    private LocalDateTime deleteDate;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private Driver driver;

    public void updateDeliveryImageUrl(String deliveryImageUrl) {
        this.deliveryImageUrl = deliveryImageUrl;
    }

    public void assignToDriver(Driver driver) {
        this.driver = driver;
        this.shippingStatus = ShippingStatus.WAITING;
    }

    public void updateShippingStatus(ShippingStatus shippingStatus) {
        this.shippingStatus = shippingStatus;
    }
}